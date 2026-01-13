package pl.dgrecki.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.InboxEvent;

@Repository
public interface InboxEventRepository extends JpaRepository<InboxEvent, UUID> {
    @Query("SELECT e FROM InboxEvent e "
            + "WHERE (e.status = :received OR (e.status = :failed AND e.attempts < e.maxAttempts)) "
            + "ORDER BY e.createdAt ASC")
    List<InboxEvent> findReadyToProcess(
            @Param("received") InboxEvent.Status received,
            @Param("failed") InboxEvent.Status failed,
            Pageable pageable);
}
