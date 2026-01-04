package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.EMAIL_EVENT_SERIALIZE_FAILED_MSG;
import static pl.dgrecki.models.entities.OutboxEvent.AggregateType.USER;
import static pl.dgrecki.models.entities.OutboxEvent.EventType.USER_REGISTRATION_MAIL;
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

    private static final String TEMPLATE = "Template_name";
    private static final String SUBJECT = "Subject_value";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        eventService = new EventService(outboxService, objectMapper, TEMPLATE, SUBJECT);
    }

    @Test
    void shouldCreateUserRegistrationMailEventTest() {
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        String activationLink = "https://example.com/";
        User user = new User(email, password, firstName, lastName, PENDING_ACTIVATION, Set.of());

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);

        eventService.createUserRegistrationMailEvent(user, activationLink);

        verify(outboxService).createOutboxEvent(eq(USER), eq(null), eq(USER_REGISTRATION_MAIL), jsonCaptor.capture());

        String json = jsonCaptor.getValue();
        assertNotNull(json);
        assertEquals(email, JsonPath.read(json, "$.to"));
        assertEquals(TEMPLATE, JsonPath.read(json, "$.template"));
        assertEquals(SUBJECT, JsonPath.read(json, "$.subject"));
        assertEquals(activationLink, JsonPath.read(json, "$.data.activationLink"));
        assertEquals(firstName, JsonPath.read(json, "$.data.userFirstName"));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenJsonSerializationFailsTest() throws Exception {
        String activationLink = "https://example.com/";
        ObjectMapper failingMapper = mock(ObjectMapper.class);

        EventService failingService = new EventService(outboxService, failingMapper, TEMPLATE, SUBJECT);

        when(failingMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Json serialize failed") {});

        User user = new User();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> failingService.createUserRegistrationMailEvent(user, activationLink));
        assertEquals(EMAIL_EVENT_SERIALIZE_FAILED_MSG, exception.getMessage());

        verify(outboxService, never()).createOutboxEvent(any(), any(), any(), any());
    }
}
