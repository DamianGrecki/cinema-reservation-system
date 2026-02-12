package pl.dgrecki.repositories;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.ReservationStatus;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    boolean existsByScreeningIdAndSeatIdAndStatusIn(
            UUID screeningId, UUID seatId, Collection<ReservationStatus> statuses);

    @Query(value = """
    SELECT *
    FROM reservations
    WHERE basket_id IN :basketIds
        AND status = :createdStatus
    ORDER BY created_at
    FOR UPDATE SKIP LOCKED
""", nativeQuery = true)
    List<Reservation> claimReservationsByBasketIds(
            @Param("basketIds") List<UUID> basketIds, @Param("createdStatus") String createdStatus);

    @Modifying(clearAutomatically = true)
    @Query(value = """
    UPDATE Reservation r
    SET r.status = :status
    WHERE r.id IN :ids
        AND r.status != :status
""")
    int setReservationsStatus(@Param("ids") List<UUID> ids, @Param("status") ReservationStatus status);

    @Query("""
    SELECT r.id
    FROM Reservation r
    WHERE r.basket.id IN :basketsIds
      AND r.status = :status
""")
    List<UUID> findReservationsIdsByBasketIdsAndStatus(
            @Param("basketsIds") List<UUID> basketIds, @Param("status") ReservationStatus status);

    @Modifying(clearAutomatically = true)
    @Query(value = """
    DELETE Reservation r
    WHERE r.id IN :ids
        AND r.status = :status
""")
    int deleteReservationsByIdAndStatus(@Param("ids") List<UUID> ids, @Param("status") ReservationStatus status);

    @Query("""
        SELECT b.id
        FROM Basket b
        LEFT JOIN Reservation r ON r.basket = b
        WHERE r.id IS NULL
            AND b.expiresAt < :instant
        ORDER BY b.expiresAt ASC
    """)
    List<UUID> findExpiredBasketsIdsWithoutReservations(@Param("instant") Instant instant, Pageable pageable);
}
