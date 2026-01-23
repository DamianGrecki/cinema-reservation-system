package pl.dgrecki.unit;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import pl.dgrecki.models.entities.OutboxEvent;
import pl.dgrecki.models.events.OutgoingEvent;
import pl.dgrecki.schedulers.OutboxPublisherScheduler;
import pl.dgrecki.services.OutboxService;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherUnitTests {

    @Mock
    private OutboxService outboxService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private OutboxPublisherScheduler publisher;

    private final String topic = "mail.registration.user";

    @BeforeEach
    void setup() {
        publisher = new OutboxPublisherScheduler(outboxService, kafkaTemplate, topic, objectMapper);
    }

    @SneakyThrows
    @Test
    void shouldPublishEventsAndMarkSent() {
        String data = "{\"data\":\"Some data\"}";
        UUID id = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.builder().id(id).data(data).build();
        when(outboxService.claimOutboxEvents(any(), anyInt())).thenReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        OutgoingEvent outgoingEvent =
                new OutgoingEvent(event.getId(), event.getEventType(), objectMapper.readTree(event.getData()));

        String message = new ObjectMapper().writeValueAsString(outgoingEvent);

        publisher.publishUserRegistrationEvents();

        verify(kafkaTemplate, times(1)).send(eq(topic), eq(id.toString()), eq(message));
        verify(outboxService, times(1)).markSent(event);
        verify(outboxService, never()).handleFailedAttempt(any(), any());
    }

    @Test
    void shouldMarkFailedWhenKafkaThrowsException() {
        String data = "{\"data\":\"Some data\"}";
        String errorMessage = "Kafka error";
        OutboxEvent event = OutboxEvent.builder().data(data).build();
        when(outboxService.claimOutboxEvents(any(), anyInt())).thenReturn(List.of(event));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenThrow(new RuntimeException(errorMessage));

        publisher.publishUserRegistrationEvents();

        verify(outboxService, times(1)).handleFailedAttempt(event, errorMessage);
        verify(outboxService, never()).markSent(event);
    }
}
