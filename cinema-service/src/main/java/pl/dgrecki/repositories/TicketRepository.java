package pl.dgrecki.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.entities.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    @Query("""
    SELECT t
    FROM Ticket t
    JOIN FETCH t.order o
    JOIN FETCH t.reservation r
    JOIN FETCH r.screening s
    JOIN FETCH s.movieVersion mv
    JOIN FETCH mv.movie
    JOIN FETCH s.cinemaHall ch
    JOIN FETCH ch.cinema
    JOIN FETCH r.seat st
    JOIN FETCH st.hallRow
    WHERE t.id = :id
    """)
    Optional<Ticket> findDataForTicketPdf(UUID id);

    Optional<Ticket> findByReservation(Reservation reservation);

    List<Ticket> findByOrderId(UUID orderId);

    @Query("""
    SELECT t
    FROM Ticket t
    JOIN FETCH t.order o
    JOIN FETCH t.reservation r
    JOIN FETCH r.screening s
    JOIN FETCH s.movieVersion mv
    JOIN FETCH mv.movie
    JOIN FETCH s.cinemaHall ch
    JOIN FETCH ch.cinema
    JOIN FETCH r.seat st
    JOIN FETCH st.hallRow
    WHERE o.customerId = :customerId
    ORDER BY o.createdAt DESC, t.createdAt ASC
    """)
    List<Ticket> findHistoryByCustomerId(UUID customerId);
}
