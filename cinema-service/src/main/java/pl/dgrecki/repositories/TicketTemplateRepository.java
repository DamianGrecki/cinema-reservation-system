package pl.dgrecki.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.TicketTemplate;

@Repository
public interface TicketTemplateRepository extends JpaRepository<TicketTemplate, Long> {
    Optional<TicketTemplate> findTicketTemplateByIsActive(boolean isActive);
}
