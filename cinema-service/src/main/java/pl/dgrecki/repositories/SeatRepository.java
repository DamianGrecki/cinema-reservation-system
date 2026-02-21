package pl.dgrecki.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.HallRow;
import pl.dgrecki.models.entities.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    @Query("SELECT s.id FROM Seat s WHERE s.hallRow = :hallRow")
    List<UUID> findIdsByHallRow(@Param("hallRow") HallRow hallRow);
}
