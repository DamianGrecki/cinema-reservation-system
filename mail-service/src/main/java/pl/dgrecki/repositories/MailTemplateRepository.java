package pl.dgrecki.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.MailTemplate;
import pl.dgrecki.models.enums.TemplateType;

@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long> {
    Optional<MailTemplate> findByTemplateTypeAndIsActiveTrue(TemplateType type);
}
