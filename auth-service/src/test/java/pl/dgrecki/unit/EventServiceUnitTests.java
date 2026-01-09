package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.EVENT_DATA_SERIALIZE_FAILED_MSG;
import static pl.dgrecki.models.enums.EventType.USER_REGISTRATION;
import static pl.dgrecki.models.enums.UserStatus.PENDING_ACTIVATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.services.EventService;
import pl.dgrecki.services.OutboxService;

@ExtendWith(MockitoExtension.class)
class EventServiceUnitTests {

    @Mock
    private OutboxService outboxService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        eventService = new EventService(outboxService, objectMapper);
    }

    @Test
    void shouldCreateUserRegistrationEventTest() {
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        String activationLink = "https://example.com/";
        User user = new User(email, password, firstName, lastName, PENDING_ACTIVATION, Set.of());

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);

        eventService.createUserRegistrationEvent(user, activationLink);

        verify(outboxService).createOutboxEvent(eq(USER_REGISTRATION), jsonCaptor.capture());

        String json = jsonCaptor.getValue();
        assertNotNull(json);
        assertEquals(email, JsonPath.read(json, "$.userEmail"));
        assertEquals(activationLink, JsonPath.read(json, "$.activationLink"));
        assertEquals(firstName, JsonPath.read(json, "$.userFirstName"));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenJsonSerializationFailsTest() throws Exception {
        String activationLink = "https://example.com/";
        ObjectMapper failingMapper = mock(ObjectMapper.class);

        EventService failingService = new EventService(outboxService, failingMapper);

        when(failingMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Json serialize failed") {});

        User user = new User();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> failingService.createUserRegistrationEvent(user, activationLink));
        assertEquals(EVENT_DATA_SERIALIZE_FAILED_MSG, exception.getMessage());

        verify(outboxService, never()).createOutboxEvent(any(), any());
    }
}
