package org.example.services;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.models.events.OutboxEvent;
import org.example.models.events.OutboxEvent.Status;
import org.example.repositories.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxRepository;

    @SneakyThrows
    @Transactional
    public void createOutboxEvent(
            OutboxEvent.AggregateType aggregateType,
            Long aggregateId,
            OutboxEvent.EventType eventType,
            String data) {

        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .data(data)
                .status(Status.PENDING)
                .createdAt(Instant.now())
                .build();
        outboxRepository.save(event);
    }

    public List<OutboxEvent> fetchByStatusAndType(OutboxEvent.Status status, OutboxEvent.EventType eventType) {
        return outboxRepository.findEventsByStatusAndType(status, eventType);
    }

    @Transactional
    public void markSent(OutboxEvent event) {
        event.setStatus(Status.SENT);
        event.setSentAt(Instant.now());
        outboxRepository.save(event);
    }

    @Transactional
    public void markFailed(OutboxEvent event) {
        event.setStatus(Status.FAILED);
        outboxRepository.save(event);
    }
}
