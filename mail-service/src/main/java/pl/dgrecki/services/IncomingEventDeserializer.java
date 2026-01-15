package pl.dgrecki.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.IncomingEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncomingEventDeserializer {

    private final ObjectMapper objectMapper;

    public IncomingEvent deserialize(String message) {
        try {
            return objectMapper.readValue(message, IncomingEvent.class);
        } catch (Exception e) {
            log.error("Message deserialize failed: {}", message, e);
            throw new RuntimeException("Invalid incoming event", e);
        }
    }
}
