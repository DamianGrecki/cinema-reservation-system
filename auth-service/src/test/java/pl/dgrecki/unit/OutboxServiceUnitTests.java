package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.models.enums.EventStatus.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.models.entities.OutboxEvent;
import pl.dgrecki.models.enums.EventStatus;
import pl.dgrecki.models.enums.EventType;
import pl.dgrecki.repositories.OutboxEventRepository;
import pl.dgrecki.services.OutboxService;

@ExtendWith(MockitoExtension.class)
class OutboxServiceUnitTests {

    @Mock
    private OutboxEventRepository outboxRepository;

    @InjectMocks
    private OutboxService outboxService;

    @Test
    void shouldCreateOutboxEventAndSaveTest() {
        EventType eventType = EventType.USER_REGISTRATION;
        String data = "{\"data\":\"Some value\"}";

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);

        outboxService.createOutboxEvent(eventType, data);

        verify(outboxRepository, times(1)).save(captor.capture());
        OutboxEvent savedEvent = captor.getValue();

        assertEquals(eventType, savedEvent.getEventType());
        assertEquals(data, savedEvent.getData());
        assertEquals(0, savedEvent.getAttempts());
        assertEquals(3, savedEvent.getMaxAttempts());
        assertEquals(PENDING, savedEvent.getStatus());
        assertNotNull(savedEvent.getCreatedAt());
    }

    @Test
    void shouldFindReadyToSendEventsTest() {
        EventType eventType = EventType.USER_REGISTRATION;
        int limit = 50;

        List<OutboxEvent> eventList = List.of(new OutboxEvent(), new OutboxEvent());
        when(outboxRepository.findReadyToSend(eventType.name(), PENDING.name(), FAILED.name(), limit))
                .thenReturn(eventList);

        List<OutboxEvent> result = outboxService.claimOutboxEvents(eventType, limit);

        assertEquals(2, result.size());
        verify(outboxRepository, times(1)).findReadyToSend(eventType.name(), PENDING.name(), FAILED.name(), limit);
    }

    @Test
    void shouldMarkEventAsSentTest() {
        OutboxEvent event = OutboxEvent.builder().status(PENDING).attempts(0).build();

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);

        outboxService.markSent(event);

        verify(outboxRepository, times(1)).save(captor.capture());
        assertEquals(EventStatus.SENT, captor.getValue().getStatus());
        assertEquals(1, captor.getValue().getAttempts());
        assertNotNull(captor.getValue().getSentAt());
    }

    @Test
    void shouldHandleFailedAttemptAndSetFailedStatusTest() {
        OutboxEvent event =
                OutboxEvent.builder().status(PENDING).attempts(0).maxAttempts(3).build();
        String errorMessage = "error message";

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);

        outboxService.handleFailedAttempt(event, errorMessage);

        assertEquals(FAILED, event.getStatus());
        verify(outboxRepository, times(1)).save(captor.capture());
        assertEquals(FAILED, captor.getValue().getStatus());
        assertEquals(1, captor.getValue().getAttempts());
        assertNull(captor.getValue().getSentAt());
    }

    @Test
    void shouldHandleFailedAttemptAndSetDeadStatusTest() {
        OutboxEvent event =
                OutboxEvent.builder().status(PENDING).attempts(2).maxAttempts(3).build();
        String errorMessage = "error message";

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);

        outboxService.handleFailedAttempt(event, errorMessage);

        assertEquals(DEAD, event.getStatus());
        verify(outboxRepository, times(1)).save(captor.capture());
        assertEquals(DEAD, captor.getValue().getStatus());
        assertEquals(3, captor.getValue().getAttempts());
        assertNull(captor.getValue().getSentAt());
    }
}
