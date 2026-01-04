package pl.dgrecki.schedulers;

import static pl.dgrecki.models.entities.OutboxEvent.EventType.USER_REGISTRATION_MAIL;
import static pl.dgrecki.models.entities.OutboxEvent.Status.PENDING;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.entities.OutboxEvent;
import pl.dgrecki.services.OutboxService;

@Component
public class OutboxPublisherScheduler {

    private final OutboxService outboxService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public OutboxPublisherScheduler(
            OutboxService outboxService,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kafka.topics.mail-registration}") String topic) {
        this.outboxService = outboxService;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Scheduled(fixedDelayString = "${kafka.poll-interval-ms}")
    public void publishUserRegistrationMailEvents() {
        List<OutboxEvent> events = outboxService.fetchByStatusAndType(PENDING, USER_REGISTRATION_MAIL);
        for (OutboxEvent event : events) {
            try {
                kafkaTemplate
                        .send(topic, String.valueOf(event.getAggregateId()), event.getData())
                        .get();
                outboxService.markSent(event);
            } catch (Exception ex) {
                outboxService.markFailed(event);
            }
        }
    }
}
