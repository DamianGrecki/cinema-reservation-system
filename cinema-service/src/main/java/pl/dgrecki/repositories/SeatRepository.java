package pl.dgrecki.repositories;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.Seat;
import pl.dgrecki.models.enums.ReservationStatus;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    @Query("""
    SELECT s.hallRow.rowNumber AS rowNumber,
           s.seatNumber AS seatNumber,
           s.id AS seatId,
           CASE WHEN res.id IS NOT NULL THEN true ELSE false END AS reserved
    FROM Seat s
    JOIN s.hallRow r
    JOIN r.cinemaHall ch
    LEFT JOIN Reservation res
        ON res.seat = s
       AND res.screening.id = :screeningId
       AND res.status IN (:reservedStatuses)
    WHERE ch.id = (
        SELECT sc.cinemaHall.id
        FROM Screening sc
        WHERE sc.id = :screeningId
    )
    ORDER BY r.rowNumber, s.seatNumber
""")
    List<RowWithSeatAndStatus> findSeatsWithRowAndStatuses(
            UUID screeningId, @Param("reservedStatuses") Set<ReservationStatus> reservedStatuses);
}
