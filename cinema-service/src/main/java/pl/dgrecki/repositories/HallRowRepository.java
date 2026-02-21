package pl.dgrecki.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.CinemaHall;
import pl.dgrecki.models.entities.HallRow;

@Repository
public interface HallRowRepository extends JpaRepository<HallRow, UUID> {
    List<HallRow> findByCinemaHall(CinemaHall cinemaHall);
}
