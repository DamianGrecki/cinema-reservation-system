package pl.dgrecki.services;

import static pl.dgrecki.models.entities.InboxEvent.Status.PROCESSING;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.models.IncomingEvent;
import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.repositories.InboxEventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboxService {

    private final InboxEventRepository inboxRepository;

    @Transactional
    public void createInboxEvent(IncomingEvent incomingEvent) {
        throwIfEventAlreadyExists(incomingEvent);
        InboxEvent event = InboxEvent.builder()
                .id(incomingEvent.getEventId())
                .eventType(incomingEvent.getEventType())
                .data(incomingEvent.getData())
                .status(InboxEvent.Status.RECEIVED)
                .attempts(0)
                .maxAttempts(3)
                .createdAt(Instant.now())
                .build();
        inboxRepository.save(event);
    }

    public void markProcessed(InboxEvent event) {
        event.setStatus(InboxEvent.Status.PROCESSED);
        event.setAttempts(event.getAttempts() + 1);
        event.setProcessedAt(Instant.now());
        inboxRepository.save(event);
    }

    public void handleFailedAttempt(InboxEvent event, String error) {
        int attempts = event.getAttempts() + 1;
        event.setAttempts(attempts);
        event.setLastError(error);
        event.setProcessedAt(Instant.now());

        if (attempts < event.getMaxAttempts()) {
            event.setStatus(InboxEvent.Status.FAILED);
        } else {
            event.setStatus(InboxEvent.Status.DEAD);
            log.warn("Event {} exceeded max attempts ({})", event.getId(), event.getMaxAttempts());
        }

        inboxRepository.save(event);
    }

    public List<InboxEvent> claimInboxEvents(int limit) {
        List<InboxEvent> events = inboxRepository.findReadyToProcess(
                InboxEvent.Status.RECEIVED, InboxEvent.Status.FAILED, Pageable.ofSize(limit));
        setEventsStatus(events, PROCESSING);
        return events;
    }

    private void setEventsStatus(List<InboxEvent> events, InboxEvent.Status status) {
        if (!events.isEmpty()) {
            List<UUID> ids = events.stream().map(InboxEvent::getId).toList();
            int updatedCount = inboxRepository.setEventsStatus(ids, status);
            log.info("Status of {} inbox events set to {}", updatedCount, status);
        }
    }

    private void throwIfEventAlreadyExists(IncomingEvent incomingEvent) {
        if (inboxRepository.findById(incomingEvent.getEventId()).isPresent()) {
            throw new ResourceAlreadyExistsException("Event already exists!");
        }
    }
}
