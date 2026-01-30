package pl.dgrecki.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.HallRow;

@Repository
public interface HallRowRepository extends JpaRepository<HallRow, UUID> {}
