package pl.dgrecki.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.InboxEventDlq;

@Repository
public interface InboxEventDlqRepository extends JpaRepository<InboxEventDlq, UUID> {}
