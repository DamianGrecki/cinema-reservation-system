package pl.dgrecki.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Simulation focused on stressing DB queries that hit the reservations table.
 * Run against a database seeded with large dataset (seed-reservations.sql).
 *
 * Targets these critical query patterns:
 * 1. Seat map (LEFT JOIN reservations) — heaviest read query
 * 2. Reservation existence check (screening_id + seat_id + status IN)
 * 3. Basket-based reservation lookup (basket_id + status)
 * 4. Concurrent writes with pessimistic locking
 *
 * Usage:
 *   mvn gatling:test -Dgatling.simulationClass=pl.dgrecki.simulations.ReservationIndexBenchmarkSimulation
 *   mvn gatling:test -Dgatling.simulationClass=pl.dgrecki.simulations.ReservationIndexBenchmarkSimulation -DusersPerSec=50
 */
public class ReservationIndexBenchmarkSimulation extends BaseSimulation {

    private static final int USERS_PER_SEC = Integer.getInteger("usersPerSec", 20);
    private static final int DURATION_SEC = Integer.getInteger("durationSec", 120);
    private static final int RAMP_SEC = Integer.getInteger("rampSec", 30);

    // =========================================================================
    // Pre-fetch screening IDs once at startup to avoid hammering GET /api/screenings
    // with 8k+ screenings per user. Each user gets a random screening ID from this pool.
    // =========================================================================

    private static final List<String> SCREENING_IDS = fetchScreeningIds();

    private static List<String> fetchScreeningIds() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/screenings"))
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<String> ids = new ArrayList<>();
            Pattern pattern = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F\\-]{36})\"");
            Matcher matcher = pattern.matcher(response.body());
            while (matcher.find()) {
                ids.add(matcher.group(1));
            }
            System.out.println(">>> Pre-fetched " + ids.size() + " screening IDs");
            return ids;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch screening IDs from " + BASE_URL, e);
        }
    }

    // Feeder that provides a random screening ID to each virtual user
    private Iterator<Map<String, Object>> screeningFeeder = Stream.generate(() -> {
        String id = SCREENING_IDS.get(ThreadLocalRandom.current().nextInt(SCREENING_IDS.size()));
        return Collections.<String, Object>singletonMap("screeningId", id);
    }).iterator();

    // =========================================================================
    // Scenario 1: Seat Map Stress
    // Targets: SeatRepository.findSeatsWithRowAndStatuses()
    // This is the heaviest query — LEFT JOIN seats with reservations filtered by status.
    // Without proper indexes this degrades fast with 1M+ reservation rows.
    // =========================================================================

    ChainBuilder seatMapStress = repeat(5).on(
            feed(screeningFeeder)
            .exec(http("GET /api/screenings/{id}/seats [seat-map-stress]")
                    .get("/api/screenings/#{screeningId}/seats")
                    .check(status().is(200)))
    );

    ScenarioBuilder seatMapScenario = scenario("1. Seat map query stress")
            .exec(seatMapStress);

    // =========================================================================
    // Scenario 2: Reservation Creation (existence check + insert)
    // Targets:
    //   - existsByScreeningIdAndSeatIdAndStatusIn() — index on (screening_id, seat_id, status)
    //   - INSERT with unique partial index validation
    // =========================================================================

    ChainBuilder findAvailableSeatForBenchmark = exec(session ->
                session.set("seatFound", false).set("retryCount", 0))
            .asLongAs(session -> !session.getBoolean("seatFound") && session.getInt("retryCount") < 10)
            .on(
                    feed(screeningFeeder)
                    .exec(http("GET /api/screenings/{id}/seats [find-seat]")
                            .get("/api/screenings/#{screeningId}/seats")
                            .check(status().is(200))
                            .check(jsonPath("$[*].seats[?(@.reserved==false)].seatId")
                                    .findAll()
                                    .optional()
                                    .saveAs("availableSeats")))
                    .exec(session -> {
                        java.util.List<String> seats = session.getList("availableSeats");
                        if (seats != null && !seats.isEmpty()) {
                            int randomIdx = ThreadLocalRandom.current().nextInt(seats.size());
                            return session.set("chosenSeatId", seats.get(randomIdx)).set("seatFound", true);
                        }
                        return session.set("retryCount", session.getInt("retryCount") + 1);
                    })
            );

    ChainBuilder reservationCreation = exec(findAvailableSeatForBenchmark)
            .doIf(session -> session.getBoolean("seatFound")).then(
                    exec(createBasket)
                    .exec(http("POST /api/reservation [create-reservation]")
                            .post("/api/reservation")
                            .body(StringBody("""
                                {
                                    "basketId": "#{basketId}",
                                    "screeningId": "#{screeningId}",
                                    "seatId": "#{chosenSeatId}"
                                }
                                """))
                            .check(status().in(201, 409)))
            );

    ScenarioBuilder reservationScenario = scenario("2. Reservation creation stress")
            .exec(reservationCreation);

    // =========================================================================
    // Scenario 3: Full Purchase Flow (basket-based queries)
    // Targets:
    //   - claimReservationsByStatusesAndBasketIds() — PESSIMISTIC_WRITE lock
    //   - findByBasketIdAndStatusIn()
    //   - setReservationsStatus() — bulk UPDATE by ids
    // =========================================================================

    ChainBuilder fullPurchaseFlow = exec(findAvailableSeatForBenchmark)
            .doIf(session -> session.getBoolean("seatFound")).then(
                    exec(createBasket)
                    .exec(http("POST /api/reservation [purchase-reserve]")
                            .post("/api/reservation")
                            .body(StringBody("""
                                {
                                    "basketId": "#{basketId}",
                                    "screeningId": "#{screeningId}",
                                    "seatId": "#{chosenSeatId}"
                                }
                                """))
                            .check(status().is(201))
                            .check(jsonPath("$.reservationId").saveAs("reservationId")))
                    .exitHereIfFailed()
                    .exec(http("GET /api/basket/{id}/pricing [basket-pricing]")
                            .get("/api/basket/#{basketId}/pricing")
                            .check(status().is(200)))
                    .exec(session -> {
                        String email = "perf_" + session.userId() + "_" + System.currentTimeMillis() + "@test.com";
                        return session.set("guestEmail", email).set("guestFirstName", "PerfUser");
                    })
                    .exec(http("POST /api/payment [purchase-pay]")
                            .post("/api/payment")
                            .body(StringBody("""
                                {
                                    "basketId": "#{basketId}",
                                    "provider": "SANDBOX",
                                    "guestEmail": "#{guestEmail}",
                                    "guestFirstName": "#{guestFirstName}"
                                }
                                """))
                            .check(status().is(201)))
            );

    ScenarioBuilder purchaseScenario = scenario("3. Full purchase flow stress")
            .exec(fullPurchaseFlow);

    // =========================================================================
    // Scenario 4: Mixed Workload (realistic production traffic)
    // 70% browsing (seat map reads) + 20% reservations + 10% full purchases
    // This reveals contention between reads and writes.
    // =========================================================================

    ScenarioBuilder mixedScenario = scenario("4. Mixed workload")
            .randomSwitch().on(
                    new Choice.WithWeight(70, exec(seatMapStress)),
                    new Choice.WithWeight(20, exec(reservationCreation)),
                    new Choice.WithWeight(10, exec(fullPurchaseFlow))
            );

    // =========================================================================
    // Setup — run all 4 scenarios in parallel
    // =========================================================================

    {
        setUp(
                seatMapScenario.injectOpen(
                        rampUsersPerSec(1).to(USERS_PER_SEC).during(RAMP_SEC),
                        constantUsersPerSec(USERS_PER_SEC).during(DURATION_SEC),
                        rampUsersPerSec(USERS_PER_SEC).to(1).during(RAMP_SEC / 2)
                ),
                reservationScenario.injectOpen(
                        nothingFor(RAMP_SEC / 2),
                        rampUsersPerSec(1).to(USERS_PER_SEC / 2).during(RAMP_SEC),
                        constantUsersPerSec(USERS_PER_SEC / 2).during(DURATION_SEC),
                        rampUsersPerSec(USERS_PER_SEC / 2).to(1).during(RAMP_SEC / 2)
                ),
                purchaseScenario.injectOpen(
                        nothingFor(RAMP_SEC),
                        rampUsersPerSec(1).to(USERS_PER_SEC / 4).during(RAMP_SEC),
                        constantUsersPerSec(USERS_PER_SEC / 4).during(DURATION_SEC),
                        rampUsersPerSec(USERS_PER_SEC / 4).to(1).during(RAMP_SEC / 2)
                ),
                mixedScenario.injectOpen(
                        nothingFor(RAMP_SEC),
                        rampUsersPerSec(1).to(USERS_PER_SEC).during(RAMP_SEC),
                        constantUsersPerSec(USERS_PER_SEC).during(DURATION_SEC),
                        rampUsersPerSec(USERS_PER_SEC).to(1).during(RAMP_SEC / 2)
                )
        )
        .protocols(httpProtocol)
        .assertions(
                // Seat map — the most common query, should be fast
                details("GET /api/screenings/{id}/seats [seat-map-stress]")
                        .responseTime().percentile3().lt(500),
                // Reservation creation — involves existence check + insert
                details("POST /api/reservation [create-reservation]")
                        .responseTime().percentile3().lt(1000),
                // Payment — triggers pessimistic lock + status updates
                details("POST /api/payment [purchase-pay]")
                        .responseTime().percentile3().lt(2000),
                // Overall failure rate
                global().failedRequests().percent().lt(10.0)
        );
    }
}
