package pl.dgrecki.services;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.models.IncomingEvent;
import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.repositories.InboxEventRepository;

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
                .createdAt(Instant.now())
                .build();
        inboxRepository.save(event);
    }

    public void markProcessed(InboxEvent event) {
        event.setStatus(InboxEvent.Status.PROCESSED);
        event.setProcessedAt(Instant.now());
        inboxRepository.save(event);
    }

    public void markFailed(InboxEvent event) {
        event.setStatus(InboxEvent.Status.FAILED);
        event.setProcessedAt(Instant.now());
        inboxRepository.save(event);
    }

    public List<InboxEvent> getReceivedInboxEvent() {
        return inboxRepository.findByStatus(InboxEvent.Status.RECEIVED);
    }

    private void throwIfEventAlreadyExists(IncomingEvent incomingEvent) {
        if (inboxRepository.findById(incomingEvent.getEventId()).isPresent()) {
            throw new ResourceAlreadyExistsException("Event already exists!");
        }
    }
}
