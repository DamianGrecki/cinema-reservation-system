package pl.dgrecki.services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.OutboxEvent;
import pl.dgrecki.models.enums.EventStatus;
import pl.dgrecki.models.enums.EventType;
import pl.dgrecki.repositories.OutboxEventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxRepository;

    @Transactional
    public void createOutboxEvent(EventType eventType, String data) {
        OutboxEvent event = OutboxEvent.builder()
                .eventType(eventType)
                .data(data)
                .status(EventStatus.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .createdAt(Instant.now())
                .build();
        outboxRepository.save(event);
    }

    public void markSent(OutboxEvent event) {
        event.setStatus(EventStatus.SENT);
        event.setAttempts(event.getAttempts() + 1);
        event.setSentAt(Instant.now());
        outboxRepository.save(event);
    }

    public void handleFailedAttempt(OutboxEvent event, String error) {
        int attempts = event.getAttempts() + 1;
        event.setAttempts(attempts);
        event.setLastError(error);

        if (attempts < event.getMaxAttempts()) {
            event.setStatus(EventStatus.FAILED);
        } else {
            event.setStatus(EventStatus.DEAD);
            log.warn("Event {} exceeded max attempts ({})", event.getId(), event.getMaxAttempts());
        }

        outboxRepository.save(event);
    }

    @Transactional
    public List<OutboxEvent> claimOutboxEvents(EventType eventType, int limit) {
        List<OutboxEvent> events = outboxRepository.findReadyToSend(
                eventType.name(), EventStatus.PENDING.name(), EventStatus.FAILED.name(), limit);
        setEventsStatus(events, EventStatus.SENDING);
        return events;
    }

    private void setEventsStatus(List<OutboxEvent> events, EventStatus status) {
        if (!events.isEmpty()) {
            List<UUID> ids = events.stream().map(OutboxEvent::getId).toList();
            int updatedCount = outboxRepository.setEventsStatus(ids, status);
            log.info("Status of {} outbox events set to {}", updatedCount, status);
        }
    }
}
