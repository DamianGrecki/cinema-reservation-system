package pl.dgrecki.services;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.models.IncomingEvent;
import pl.dgrecki.models.entities.InboxEventDlq;
import pl.dgrecki.models.enums.EventStatus;
import pl.dgrecki.repositories.InboxEventDlqRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboxDlqService {

    private final InboxEventDlqRepository inboxDlqRepository;

    @Transactional
    public void createInboxDlqEvent(IncomingEvent incomingEvent) {
        throwIfEventAlreadyExists(incomingEvent);
        InboxEventDlq event = InboxEventDlq.builder()
                .id(incomingEvent.getEventId())
                .eventType(incomingEvent.getEventType())
                .data(incomingEvent.getData())
                .status(EventStatus.RECEIVED)
                .createdAt(Instant.now())
                .build();
        inboxDlqRepository.save(event);
    }

    private void throwIfEventAlreadyExists(IncomingEvent incomingEvent) {
        if (inboxDlqRepository.findById(incomingEvent.getEventId()).isPresent()) {
            throw new ResourceAlreadyExistsException("Event in inbox_events_dlq already exists!");
        }
    }
}
