package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;

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
            throw new DeserializationException(INVALID_INCOMING_EVENT_MSG, e);
        }
    }

    private void validate(IncomingEvent event) throws DeserializationException {
        if (event == null) {
            throw new DeserializationException(NULL_INCOMING_EVENT_MSG);
        }
        if (event.getEventId() == null) {
            throw new DeserializationException(MISSING_REQUIRED_FIELD_EVENT_ID_MSG);
        }
        if (event.getEventType() == null) {
            throw new DeserializationException(MISSING_REQUIRED_FIELD_EVENT_TYPE_MSG);
        }
    }
}
