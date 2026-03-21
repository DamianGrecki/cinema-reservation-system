package pl.dgrecki.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.Screening;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, UUID> {

    @Query("""
    SELECT s FROM Screening s
    JOIN FETCH s.cinemaHall
    JOIN FETCH s.movieVersion mv
    JOIN FETCH mv.movie
    """)
    List<Screening> findAllWithDetails();
}
