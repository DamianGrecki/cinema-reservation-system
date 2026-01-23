package pl.dgrecki.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.models.IncomingEvent;
import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.models.enums.EventStatus;
import pl.dgrecki.models.enums.EventType;
import pl.dgrecki.repositories.InboxEventRepository;
import pl.dgrecki.services.InboxService;

@ExtendWith(MockitoExtension.class)
class InboxServiceUnitTests {

    @Mock
    private InboxEventRepository inboxRepository;

    @InjectMocks
    private InboxService inboxService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Test
    void shouldCreateInboxEventSuccessfullyTest() {
        UUID eventId = UUID.randomUUID();

        String json = "{\"email\":\"test@example.com\"}";

        JsonNode data = objectMapper.readTree(json);
        IncomingEvent incomingEvent = new IncomingEvent(eventId, EventType.USER_REGISTRATION, data);

        when(inboxRepository.findById(eventId)).thenReturn(Optional.empty());

        inboxService.createInboxEvent(incomingEvent);

        ArgumentCaptor<InboxEvent> captor = ArgumentCaptor.forClass(InboxEvent.class);
        verify(inboxRepository, times(1)).save(captor.capture());

        InboxEvent savedEvent = captor.getValue();
        assertEquals(eventId, savedEvent.getId());
        assertEquals(EventType.USER_REGISTRATION, savedEvent.getEventType());
        assertEquals(EventStatus.RECEIVED, savedEvent.getStatus());
        assertEquals(0, savedEvent.getAttempts());
        assertEquals(3, savedEvent.getMaxAttempts());
        assertNotNull(savedEvent.getCreatedAt());
    }

    @SneakyThrows
    @Test
    void shouldThrowExceptionWhenInboxEventAlreadyExistsTest() {
        UUID eventId = UUID.randomUUID();

        String json = "{\"email\":\"test@example.com\"}";

        JsonNode data = objectMapper.readTree(json);
        IncomingEvent incomingEvent = new IncomingEvent(eventId, EventType.USER_REGISTRATION, data);

        when(inboxRepository.findById(eventId)).thenReturn(Optional.of(new InboxEvent()));

        assertThrows(ResourceAlreadyExistsException.class, () -> inboxService.createInboxEvent(incomingEvent));

        verify(inboxRepository, never()).save(any());
    }

    @Test
    void shouldMarkEventAsProcessedTest() {
        InboxEvent event =
                InboxEvent.builder().status(EventStatus.PROCESSING).attempts(1).build();

        inboxService.markProcessed(event);

        assertEquals(EventStatus.PROCESSED, event.getStatus());
        assertEquals(2, event.getAttempts());
        assertNotNull(event.getProcessedAt());
        verify(inboxRepository, times(1)).save(event);
    }

    @Test
    void shouldMarkEventAsFailedWhenAttemptsBelowMaxTest() {
        String errorMessage = "error message";
        InboxEvent event = InboxEvent.builder()
                .attempts(1)
                .maxAttempts(3)
                .status(EventStatus.PROCESSING)
                .build();

        inboxService.handleFailedAttempt(event, errorMessage);

        assertEquals(2, event.getAttempts());
        assertEquals(EventStatus.FAILED, event.getStatus());
        assertEquals(errorMessage, event.getLastError());
        assertNotNull(event.getProcessedAt());
        verify(inboxRepository, times(1)).save(event);
    }

    @Test
    void shouldMarkEventAsDeadWhenMaxAttemptsReachedTest() {
        String errorMessage = "error message";
        InboxEvent event = InboxEvent.builder()
                .id(UUID.randomUUID())
                .attempts(2)
                .maxAttempts(3)
                .status(EventStatus.PROCESSING)
                .build();

        inboxService.handleFailedAttempt(event, errorMessage);

        assertEquals(3, event.getAttempts());
        assertEquals(EventStatus.DEAD, event.getStatus());
        assertEquals(errorMessage, event.getLastError());
        verify(inboxRepository, times(1)).save(event);
    }

    @Test
    void shouldClaimInboxEventsAndSetProcessingStatusTest() {
        InboxEvent event1 = InboxEvent.builder().id(UUID.randomUUID()).build();
        InboxEvent event2 = InboxEvent.builder().id(UUID.randomUUID()).build();

        when(inboxRepository.findReadyToProcess(EventStatus.RECEIVED.name(), EventStatus.FAILED.name(), 10))
                .thenReturn(List.of(event1, event2));

        when(inboxRepository.setEventsStatus(List.of(event1.getId(), event2.getId()), EventStatus.PROCESSING))
                .thenReturn(2);

        List<InboxEvent> result = inboxService.claimInboxEvents(10);

        assertEquals(2, result.size());
        verify(inboxRepository, times(1))
                .setEventsStatus(List.of(event1.getId(), event2.getId()), EventStatus.PROCESSING);
    }

    @Test
    void shouldReturnEmptyListWhenNoEventsToClaimTest() {
        when(inboxRepository.findReadyToProcess(any(), any(), anyInt())).thenReturn(List.of());

        List<InboxEvent> result = inboxService.claimInboxEvents(5);

        assertTrue(result.isEmpty());
        verify(inboxRepository, never()).setEventsStatus(any(), any());
    }
}
