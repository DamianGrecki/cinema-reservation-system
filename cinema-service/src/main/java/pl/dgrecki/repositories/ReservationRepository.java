package pl.dgrecki.repositories;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
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
    WHERE status = :createdStatus
        AND expires_at < CURRENT_TIMESTAMP
    ORDER BY expires_at
    FOR UPDATE SKIP LOCKED
    LIMIT :limit
""", nativeQuery = true)
    List<Reservation> claimOverdueReservations(@Param("createdStatus") String createdStatus, @Param("limit") int limit);

    @Modifying(clearAutomatically = true)
    @Query(value = """
    UPDATE Reservation r
        SET r.status = :status
        WHERE r.id IN :ids
        AND r.status != :status
""")
    int setReservationsStatus(@Param("ids") List<UUID> ids, @Param("status") ReservationStatus status);
}
