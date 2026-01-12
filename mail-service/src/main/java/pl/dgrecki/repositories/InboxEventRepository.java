package pl.dgrecki.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.InboxEvent;

@Repository
public interface InboxEventRepository extends JpaRepository<InboxEvent, UUID> {
    @Query("SELECT e FROM InboxEvent e WHERE e.status = :status ORDER BY e.createdAt ASC")
    List<InboxEvent> findByStatus(@Param("status") InboxEvent.Status status);
}
