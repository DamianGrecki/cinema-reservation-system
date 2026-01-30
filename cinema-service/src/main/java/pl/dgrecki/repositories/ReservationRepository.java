package pl.dgrecki.repositories;

import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.ReservationStatus;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    boolean existsByScreeningIdAndSeatIdAndStatusIn(
            UUID screeningId, UUID seatId, Collection<ReservationStatus> statuses);
}
