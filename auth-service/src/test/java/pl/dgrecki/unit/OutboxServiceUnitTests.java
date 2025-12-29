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
import pl.dgrecki.models.events.OutboxEvent;
import pl.dgrecki.models.events.OutboxEvent.AggregateType;
import pl.dgrecki.models.events.OutboxEvent.EventType;
import pl.dgrecki.models.events.OutboxEvent.Status;
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
        AggregateType aggregateType = AggregateType.USER;
        Long aggregateId = 1L;
        EventType eventType = EventType.USER_REGISTRATION_MAIL;
        String data = "{\"data\":\"Some value\"}";

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);

        outboxService.createOutboxEvent(aggregateType, aggregateId, eventType, data);

        verify(outboxRepository, times(1)).save(captor.capture());
        OutboxEvent savedEvent = captor.getValue();

        assertEquals(aggregateType, savedEvent.getAggregateType());
        assertEquals(aggregateId, savedEvent.getAggregateId());
        assertEquals(eventType, savedEvent.getEventType());
        assertEquals(data, savedEvent.getData());
        assertEquals(Status.PENDING, savedEvent.getStatus());
        assertNotNull(savedEvent.getCreatedAt());
    }

    @Test
    void shouldFetchEventsByStatusAndTypeTest() {
        EventType eventType = EventType.USER_REGISTRATION_MAIL;
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
