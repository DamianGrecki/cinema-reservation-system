package pl.dgrecki.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.dgrecki.exceptions.DeserializationException;
import pl.dgrecki.models.IncomingEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncomingEventDeserializer {

    private final ObjectMapper objectMapper;

    public IncomingEvent deserialize(String message) {
        try {
            IncomingEvent event = objectMapper.readValue(message, IncomingEvent.class);
            validate(event);
            return event;
        } catch (Exception e) {
            log.error("Message deserialize failed: {}", message, e);
            throw new DeserializationException("Invalid incoming event", e);
        }
    }

    private void validate(IncomingEvent event) throws DeserializationException {
        if (event.getEventId() == null) {
            throw new DeserializationException("Missing required field: eventId");
        }
        if (event.getEventType() == null) {
            throw new DeserializationException("Missing required field: eventType");
        }
    }
}
