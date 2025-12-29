package org.example.unit;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.example.models.events.OutboxEvent;
import org.example.schedulers.OutboxPublisherScheduler;
import org.example.services.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherSchedulerTests {

    @Mock
    private OutboxService outboxService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OutboxPublisherScheduler publisher;

    private final String topic = "mail.registration.user";

    @BeforeEach
    void setup() {
        publisher = new OutboxPublisherScheduler(outboxService, kafkaTemplate, topic);
    }

    @Test
    void shouldPublishEventsAndMarkSent() {
        String data = "{\"data\":\"Some data\"}";
        OutboxEvent event = OutboxEvent.builder().aggregateId(1L).data(data).build();
        when(outboxService.fetchByStatusAndType(any(), any())).thenReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        publisher.publishUserRegistrationMailEvents();

        verify(kafkaTemplate, times(1)).send(eq(topic), eq("1"), eq(data));
        verify(outboxService, times(1)).markSent(event);
        verify(outboxService, never()).markFailed(event);
    }

    @Test
    void shouldMarkFailedWhenKafkaThrowsException() {
        String data = "{\"data\":\"Some data\"}";
        OutboxEvent event = OutboxEvent.builder().aggregateId(1L).data(data).build();
        when(outboxService.fetchByStatusAndType(any(), any())).thenReturn(List.of(event));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Kafka error"));

        publisher.publishUserRegistrationMailEvents();

        verify(outboxService, times(1)).markFailed(event);
        verify(outboxService, never()).markSent(event);
    }
}
