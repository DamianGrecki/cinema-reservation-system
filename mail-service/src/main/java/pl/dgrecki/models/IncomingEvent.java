package pl.dgrecki.models;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.dgrecki.models.enums.EventType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IncomingEvent {
    private UUID eventId;
    private EventType eventType;
    private JsonNode data;
}
