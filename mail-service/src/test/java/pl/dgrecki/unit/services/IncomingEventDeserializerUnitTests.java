package pl.dgrecki.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static pl.dgrecki.constants.ExceptionMessages.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.dgrecki.exceptions.DeserializationException;
import pl.dgrecki.models.IncomingEvent;
import pl.dgrecki.models.enums.EventType;
import pl.dgrecki.services.IncomingEventDeserializer;

class IncomingEventDeserializerUnitTests {

    private IncomingEventDeserializer deserializer;

    @BeforeEach
    void setUp() {
        deserializer = new IncomingEventDeserializer(new ObjectMapper());
    }

    @Test
    void shouldDeserializeValidIncomingEventTest() {
        UUID eventId = UUID.randomUUID();
        String json = """
                {
                  "eventId": "%s",
                  "eventType": "USER_REGISTRATION",
                  "data": { "email": "test@example.com" }
                }
                """.formatted(eventId);

        IncomingEvent event = deserializer.deserialize(json);

        assertEquals(eventId, event.getEventId());
        assertEquals(EventType.USER_REGISTRATION, event.getEventType());
        assertNotNull(event.getData());
        assertEquals("test@example.com", event.getData().get("email").asText());
    }

    @Test
    void shouldThrowExceptionWhenJsonIsInvalidTest() {
        String invalidJson = "{ not a json object} }";

        DeserializationException ex =
                assertThrows(DeserializationException.class, () -> deserializer.deserialize(invalidJson));

        assertEquals(INVALID_INCOMING_EVENT_MSG, ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    void shouldThrowExceptionWhenEventIdIsMissingTest() {
        String jsonWithoutEventId = """
                {
                  "eventType": "USER_REGISTRATION",
                  "data": {}
                }
                """;

        DeserializationException ex =
                assertThrows(DeserializationException.class, () -> deserializer.deserialize(jsonWithoutEventId));

        assertEquals(INVALID_INCOMING_EVENT_MSG, ex.getMessage());
        assertEquals(MISSING_REQUIRED_FIELD_EVENT_ID_MSG, ex.getCause().getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEventTypeIsMissingTest() {
        String jsonWithoutEventType = """
                {
                  "eventId": "%s",
                  "data": {}
                }
                """.formatted(UUID.randomUUID());

        DeserializationException ex =
                assertThrows(DeserializationException.class, () -> deserializer.deserialize(jsonWithoutEventType));

        assertEquals(INVALID_INCOMING_EVENT_MSG, ex.getMessage());
        assertEquals(MISSING_REQUIRED_FIELD_EVENT_TYPE_MSG, ex.getCause().getMessage());
    }
}
