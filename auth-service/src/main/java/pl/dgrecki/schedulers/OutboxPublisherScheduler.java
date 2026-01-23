package pl.dgrecki.schedulers;

import static pl.dgrecki.models.enums.EventType.USER_REGISTRATION;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.entities.OutboxEvent;
import pl.dgrecki.models.events.OutgoingEvent;
import pl.dgrecki.services.OutboxService;

@Component
public class OutboxPublisherScheduler {
    private static final int EVENTS_LIMIT = 50;

    private final OutboxService outboxService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;
    private final ObjectMapper objectMapper;

    public OutboxPublisherScheduler(
            OutboxService outboxService,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${spring.kafka.topics.user-registration}") String topic,
            ObjectMapper objectMapper) {
        this.outboxService = outboxService;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${spring.kafka.poll-interval-ms}")
    public void publishUserRegistrationEvents() {
        List<OutboxEvent> events = outboxService.claimOutboxEvents(USER_REGISTRATION, EVENTS_LIMIT);
        for (OutboxEvent event : events) {
            try {
                OutgoingEvent outgoingEvent =
                        new OutgoingEvent(event.getId(), event.getEventType(), objectMapper.readTree(event.getData()));
                String message = objectMapper.writeValueAsString(outgoingEvent);
                kafkaTemplate
                        .send(topic, String.valueOf(event.getId()), message)
                        .thenAccept(_ -> outboxService.markSent(event))
                        .exceptionally(ex -> {
                            outboxService.handleFailedAttempt(event, ex.getMessage());
                            return null;
                        });
            } catch (Exception ex) {
                outboxService.handleFailedAttempt(event, ex.getMessage());
            }
        }
    }
}
