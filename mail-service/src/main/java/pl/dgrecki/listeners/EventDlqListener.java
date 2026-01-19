package pl.dgrecki.listeners;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.IncomingEvent;
import pl.dgrecki.services.InboxDlqService;
import pl.dgrecki.services.IncomingEventDeserializer;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDlqListener {

    private final IncomingEventDeserializer incomingEventDeserializer;
    private final InboxDlqService inboxDlqService;

    @SneakyThrows
    @KafkaListener(topics = "${kafka.topics.user-registration-dlq}")
    public void handleIncomingEvent(String message, Acknowledgment acknowledgment) {
        IncomingEvent incomingEvent = incomingEventDeserializer.deserialize(message);
        inboxDlqService.createInboxDlqEvent(incomingEvent);
        acknowledgment.acknowledge();
    }
}
