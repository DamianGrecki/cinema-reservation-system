package pl.dgrecki.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.models.entities.InboxEventDlq;
import pl.dgrecki.repositories.InboxEventDlqRepository;
import pl.dgrecki.services.InboxDlqService;

@ExtendWith(MockitoExtension.class)
class InboxDlqServiceUnitTests {

    @Mock
    private InboxEventDlqRepository inboxDlqRepository;

    @InjectMocks
    private InboxDlqService inboxDlqService;

    @Test
    void shouldCreateInboxDlqEventSuccessfullyTest() {
        String topic = "mail.registration.user";
        int partition = 2;
        String rawMessage = "{\"email\":\"test@example.com\"}";
        String errorMessage = "error message";

        inboxDlqService.createInboxDlqEvent(topic, partition, rawMessage, errorMessage);

        ArgumentCaptor<InboxEventDlq> captor = ArgumentCaptor.forClass(InboxEventDlq.class);

        verify(inboxDlqRepository, times(1)).save(captor.capture());

        InboxEventDlq savedEvent = captor.getValue();

        assertEquals(topic, savedEvent.getTopic());
        assertEquals(partition, savedEvent.getPartition());
        assertEquals(rawMessage, savedEvent.getRawMessage());
        assertEquals(errorMessage, savedEvent.getErrorMessage());
        assertNotNull(savedEvent.getCreatedAt());

        assertTrue(savedEvent.getCreatedAt().isBefore(Instant.now().plusSeconds(1)));
    }
}
