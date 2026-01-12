package pl.dgrecki.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.IncomingEvent;
import pl.dgrecki.services.InboxService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

    private final InboxService inboxService;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @KafkaListener(topics = "${kafka.topics.user-registration}")
    public void handleIncomingEvent(String message) {
        IncomingEvent incomingEvent = deserializeMessage(message);
        inboxService.createInboxEvent(incomingEvent);
    }

    private IncomingEvent deserializeMessage(String message) {
        try {
            return objectMapper.readValue(message, IncomingEvent.class);
        } catch (Exception e) {
            log.error("Message deserialize failed: {}", message, e);
            throw new RuntimeException(e);
        }
    }
}
