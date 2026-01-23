package pl.dgrecki.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.dgrecki.models.entities.OutboxEvent;
import pl.dgrecki.models.enums.EventStatus;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    @Query(value = """
        SELECT *
        FROM outbox_events
        WHERE (
            (status = :pending OR (status = :failed AND attempts < max_attempts))
            AND event_type = :eventType
        )
        ORDER BY created_at
        FOR UPDATE SKIP LOCKED
        LIMIT :limit
    """, nativeQuery = true)
    List<OutboxEvent> findReadyToSend(
            @Param("eventType") String eventType,
            @Param("pending") String pending,
            @Param("failed") String failed,
            @Param("limit") int limit);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE OutboxEvent e
        SET e.status = :status
        WHERE e.id IN :ids
        AND e.status != :status
    """)
    int setEventsStatus(@Param("ids") List<UUID> ids, @Param("status") EventStatus status);
}
