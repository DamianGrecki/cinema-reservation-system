package org.example.repositories;

import java.util.List;
import java.util.UUID;
import org.example.models.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status AND e.eventType = :eventType ORDER BY e.createdAt ASC")
    List<OutboxEvent> findEventsByStatusAndType(
            @Param("status") OutboxEvent.Status status, @Param("eventType") OutboxEvent.EventType eventType);
}
