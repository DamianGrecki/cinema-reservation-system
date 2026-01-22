package pl.dgrecki.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.IncomingEvent;
import pl.dgrecki.services.InboxService;
import pl.dgrecki.services.IncomingEventDeserializer;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

    private final InboxService inboxService;
    private final IncomingEventDeserializer incomingEventDeserializer;

    @KafkaListener(topics = "${kafka.topics.user-registration}")
    public void handleIncomingEvent(String message, Acknowledgment acknowledgment) {
        IncomingEvent incomingEvent = incomingEventDeserializer.deserialize(message);
        inboxService.createInboxEvent(incomingEvent);
        acknowledgment.acknowledge();
    }
}
