package pl.dgrecki.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.models.enums.EventStatus;

@Repository
public interface InboxEventRepository extends JpaRepository<InboxEvent, UUID> {
    @Query(value = """
        SELECT *
        FROM inbox_events
        WHERE (
            status = :received
            OR (status = :failed AND attempts < max_attempts)
        )
        ORDER BY created_at
        FOR UPDATE SKIP LOCKED
        LIMIT :limit
    """, nativeQuery = true)
    List<InboxEvent> findReadyToProcess(
            @Param("received") String received, @Param("failed") String failed, @Param("limit") int limit);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE InboxEvent e
        SET e.status = :status
        WHERE e.id IN :ids
        AND e.status != :status
    """)
    int setEventsStatus(@Param("ids") List<UUID> ids, @Param("status") EventStatus status);
}
