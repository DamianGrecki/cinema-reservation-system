package org.example.repositories;

import java.util.List;
import java.util.UUID;
import org.example.models.OutboxEvent;
import org.example.models.OutboxEvent.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatus(Status status);
}
