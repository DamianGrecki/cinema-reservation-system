package pl.dgrecki.models.events;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.dgrecki.models.enums.EventType;

@Data
@AllArgsConstructor
public class OutgoingEvent {
    private UUID eventId;
    private EventType eventType;
    private JsonNode data;
}
