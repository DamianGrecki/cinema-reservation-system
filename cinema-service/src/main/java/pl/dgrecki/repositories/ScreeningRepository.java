package pl.dgrecki.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.Screening;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, UUID> {

    @Query("""
    SELECT s FROM Screening s
    JOIN FETCH s.cinemaHall
    JOIN FETCH s.movieVersion mv
    JOIN FETCH mv.movie
    WHERE s.id = :id
    """)
    Optional<Screening> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
    SELECT s FROM Screening s
    JOIN FETCH s.cinemaHall
    JOIN FETCH s.movieVersion mv
    JOIN FETCH mv.movie
    WHERE s.startTime >= :startOfDay AND s.startTime < :endOfDay
    ORDER BY s.startTime
    """)
    List<Screening> findAllByDateWithDetails(
            @Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);
}
