package org.example.schedulers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.models.OutboxEvent;
import org.example.services.OutboxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisherScheduler {

    private final OutboxService outboxService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${outbox.kafka.topic}")
    private String topic;

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms}")
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxService.fetchPending();
        for (OutboxEvent event : events) {
            try {
                kafkaTemplate
                        .send(topic, String.valueOf(event.getAggregateId()), event.getPayload())
                        .get();
                outboxService.markSent(event);
            } catch (Exception ex) {
                outboxService.markFailed(event);
            }
        }
    }
}
