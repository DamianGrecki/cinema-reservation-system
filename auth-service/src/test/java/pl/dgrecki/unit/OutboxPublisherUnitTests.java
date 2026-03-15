package pl.dgrecki.unit;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import pl.dgrecki.models.entities.OutboxEvent;
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
    private final String hmacSecret = "test-hmac-secret-for-unit-tests";

    @BeforeEach
    void setup() {
        publisher = new OutboxPublisherScheduler(outboxService, kafkaTemplate, topic, objectMapper, hmacSecret);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Test
    void shouldPublishEventsAndMarkSent() {
        String data = "{\"data\":\"Some data\"}";
        UUID id = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.builder().id(id).data(data).build();
        when(outboxService.claimOutboxEvents(any(), anyInt())).thenReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        publisher.publishUserRegistrationEvents();

        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
        verify(outboxService, times(1)).markSent(event);
        verify(outboxService, never()).handleFailedAttempt(any(), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldMarkFailedWhenKafkaThrowsException() {
        String data = "{\"data\":\"Some data\"}";
        String errorMessage = "Kafka error";
        OutboxEvent event = OutboxEvent.builder().data(data).build();
        when(outboxService.claimOutboxEvents(any(), anyInt())).thenReturn(List.of(event));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenThrow(new RuntimeException(errorMessage));

        publisher.publishUserRegistrationEvents();

        verify(outboxService, times(1)).handleFailedAttempt(event, errorMessage);
        verify(outboxService, never()).markSent(event);
    }
}
