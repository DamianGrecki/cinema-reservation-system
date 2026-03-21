-- =============================================================================
-- Performance test data generator for reservations
-- Usage: psql -v count=1000000 -f seed-reservations.sql
-- Or:    psql -f seed-reservations.sql  (defaults to 1,000,000)
-- Clean: psql -f seed-reservations.sql -v clean=1
--
-- Status distribution (realistic production-like mix):
--   ~40% PAID, ~25% EXPIRED, ~20% CANCELED, ~10% CREATED, ~5% REFUNDED
-- The unique partial index on (screening_id, seat_id) covers CREATED/PENDING_PAYMENT/PAID.
-- To respect it, we generate fewer screenings and allow multiple reservation
-- "rounds" per seat (historical EXPIRED/CANCELED + at most one active).
-- =============================================================================

-- Default to 1M if not provided
\if :{?count}
\else
    \set count 1000000
\endif

-- Clean mode: remove all test data and exit
\if :{?clean}
    BEGIN;
    DELETE FROM reservations WHERE basket_id IN (SELECT id FROM baskets WHERE expires_at = '2099-01-01 00:00:00+00');
    DELETE FROM baskets WHERE expires_at = '2099-01-01 00:00:00+00';
    -- After deleting reservations, test screenings have no reservations left — delete orphans
    DELETE FROM screenings s
    WHERE NOT EXISTS (SELECT 1 FROM reservations r WHERE r.screening_id = s.id);
    COMMIT;
    \echo '>>> Cleaned up all test data.'
    \quit
\endif

\echo '>>> Generating' :count 'reservations for performance testing...'
\timing on

BEGIN;

-- ============================================================================
-- 1. Generate screenings
--    We use ~4 rounds of reservations per screening×seat pair.
--    Rounds 1-3: EXPIRED/CANCELED (safe for unique index)
--    Round 4:    mix of PAID/CREATED (one active per seat)
--    So we need ceil(:count / (64 * 4)) screenings batches × 2 halls.
-- ============================================================================

CREATE TEMP TABLE tmp_movie_versions AS
SELECT id, ROW_NUMBER() OVER () AS rn
FROM movie_versions;

-- seats_per_screening = 32 (per hall), 2 halls = 64 total
-- rounds_per_seat = 4 → effective capacity = 64 * 4 = 256 reservations per screening-pair
-- screenings needed = ceil(:count / 256)

CREATE TEMP TABLE tmp_new_screenings AS
SELECT
    gen_random_uuid() AS id,
    ch.cinema_hall_id,
    mv.id AS movie_version_id,
    -- Screenings start 1 day in the future, spaced 3h apart.
    -- Future dates pass validateShowStartTime (allows reservation up to 30min after start).
    now() + INTERVAL '1 day' + (s.n * INTERVAL '3 hours') AS start_time,
    now() + INTERVAL '1 day' + (s.n * INTERVAL '3 hours') + INTERVAL '2 hours' AS end_time
FROM (
    SELECT generate_series(0, (:count + 255) / 256 - 1) AS n
) s
CROSS JOIN (
    VALUES (1::bigint), (2::bigint)
) ch(cinema_hall_id)
JOIN tmp_movie_versions mv ON mv.rn = (s.n % (SELECT COUNT(*) FROM tmp_movie_versions)) + 1;

\echo '>>> Inserting screenings...'

INSERT INTO screenings (id, cinema_hall_id, movie_version_id, start_time, end_time)
SELECT id, cinema_hall_id, movie_version_id, start_time, end_time
FROM tmp_new_screenings;

-- ============================================================================
-- 2. Generate baskets (1 basket per ~5 reservations)
-- ============================================================================

\echo '>>> Inserting baskets...'

CREATE TEMP TABLE tmp_baskets AS
SELECT gen_random_uuid() AS id
FROM generate_series(1, (:count + 4) / 5);

INSERT INTO baskets (id, expires_at, created_at)
SELECT id, '2099-01-01 00:00:00+00'::timestamptz, now()
FROM tmp_baskets;

-- ============================================================================
-- 3. Generate reservations
--    For each screening×seat we create up to 4 rounds:
--      round 1: EXPIRED    (historical)
--      round 2: CANCELED   (historical)
--      round 3: EXPIRED    (historical)
--      round 4: PAID/CREATED/REFUNDED (active — only 1 per unique index)
--
--    Round 4 only covers 80% of seats — the remaining 20% stay free
--    so performance test scenarios can find and reserve them.
-- ============================================================================

\echo '>>> Building reservation candidates...'

CREATE TEMP TABLE tmp_baskets_numbered AS
SELECT id, ROW_NUMBER() OVER () AS rn FROM tmp_baskets;

-- Build the cartesian product: screening × seats_in_that_hall × rounds
-- Round 4 (active reservations) only covers ~80% of seats per screening,
-- leaving ~20% free for performance test scenarios to reserve.
CREATE TEMP TABLE tmp_seat_numbered AS
SELECT
    sc.id AS screening_id,
    sc.cinema_hall_id,
    s.id AS seat_id,
    ROW_NUMBER() OVER (PARTITION BY sc.id ORDER BY hr.id, s.seat_number) AS seat_pos,
    COUNT(*) OVER (PARTITION BY sc.id) AS total_seats
FROM tmp_new_screenings sc
JOIN hall_rows hr ON hr.cinema_hall_id = sc.cinema_hall_id
JOIN seats s ON s.hall_row_id = hr.id;

CREATE TEMP TABLE tmp_reservation_candidates AS
SELECT
    ROW_NUMBER() OVER () AS rn,
    sn.screening_id,
    sn.seat_id,
    r.round_num
FROM tmp_seat_numbered sn
CROSS JOIN (VALUES (1), (2), (3), (4)) r(round_num)
-- Rounds 1-3 (historical): all seats. Round 4 (active): first 80% of seats only.
WHERE r.round_num <= 3 OR sn.seat_pos <= (sn.total_seats * 80 / 100)
ORDER BY sn.screening_id, sn.seat_id, r.round_num;

\echo '>>> Inserting reservations (this may take a moment)...'

INSERT INTO reservations (id, basket_id, screening_id, seat_id, status, pricing_type, created_at, version)
SELECT
    gen_random_uuid(),
    b.id,
    rc.screening_id,
    rc.seat_id,
    CASE rc.round_num
        WHEN 1 THEN 'EXPIRED'
        WHEN 2 THEN 'CANCELED'
        WHEN 3 THEN 'EXPIRED'
        WHEN 4 THEN
            -- Within round 4: 75% PAID, 12.5% CREATED, 12.5% REFUNDED
            CASE (rc.rn % 8)
                WHEN 0 THEN 'CREATED'
                WHEN 7 THEN 'REFUNDED'
                ELSE 'PAID'
            END
    END,
    CASE WHEN rc.rn % 3 = 0 THEN 'REDUCED' ELSE 'NORMAL' END,
    -- All created_at within last hour — prevents scheduler from deleting EXPIRED rows
    now() - (rc.rn % 3600) * INTERVAL '1 second',
    0
FROM tmp_reservation_candidates rc
JOIN tmp_baskets_numbered b ON b.rn = ((rc.rn - 1) / 5) + 1
WHERE rc.rn <= :count;

-- ============================================================================
-- 4. Cleanup temp tables and report
-- ============================================================================

DROP TABLE tmp_movie_versions;
DROP TABLE tmp_new_screenings;
DROP TABLE tmp_baskets;
DROP TABLE tmp_baskets_numbered;
DROP TABLE tmp_seat_numbered;
DROP TABLE tmp_reservation_candidates;

COMMIT;

\echo '>>> Done! Summary:'
SELECT
    COUNT(*) AS total_reservations,
    COUNT(*) FILTER (WHERE status = 'PAID') AS paid,
    COUNT(*) FILTER (WHERE status = 'CREATED') AS created,
    COUNT(*) FILTER (WHERE status = 'EXPIRED') AS expired,
    COUNT(*) FILTER (WHERE status = 'CANCELED') AS canceled,
    COUNT(*) FILTER (WHERE status = 'REFUNDED') AS refunded
FROM reservations;

SELECT
    (SELECT COUNT(*) FROM screenings) AS total_screenings,
    (SELECT COUNT(*) FROM baskets) AS total_baskets;
