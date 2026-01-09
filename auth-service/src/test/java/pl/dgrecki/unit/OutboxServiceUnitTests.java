package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.models.entities.OutboxEvent;
import pl.dgrecki.models.entities.OutboxEvent.Status;
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
        assertEquals(Status.PENDING, savedEvent.getStatus());
        assertNotNull(savedEvent.getCreatedAt());
    }

    @Test
    void shouldFetchEventsByStatusAndTypeTest() {
        EventType eventType = EventType.USER_REGISTRATION;
        Status status = Status.PENDING;

        List<OutboxEvent> eventList = List.of(new OutboxEvent(), new OutboxEvent());
        when(outboxRepository.findEventsByStatusAndType(status, eventType)).thenReturn(eventList);

        List<OutboxEvent> result = outboxService.fetchByStatusAndType(status, eventType);

        assertEquals(2, result.size());
        verify(outboxRepository, times(1)).findEventsByStatusAndType(status, eventType);
    }

    @Test
    void shouldMarkEventAsSentTest() {
        OutboxEvent event = OutboxEvent.builder().status(Status.PENDING).build();

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);

        outboxService.markSent(event);

        verify(outboxRepository, times(1)).save(captor.capture());
        assertEquals(Status.SENT, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getSentAt());
    }

    @Test
    void shouldMarkEventAsFailedTest() {
        OutboxEvent event = OutboxEvent.builder().status(Status.PENDING).build();

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);

        outboxService.markFailed(event);

        assertEquals(Status.FAILED, event.getStatus());
        verify(outboxRepository, times(1)).save(captor.capture());
        assertEquals(Status.FAILED, captor.getValue().getStatus());
        assertNull(captor.getValue().getSentAt());
    }
}
