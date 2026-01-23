package pl.dgrecki.unit.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.handlers.UserRegistrationHandler;
import pl.dgrecki.models.MailData;
import pl.dgrecki.models.UserRegistrationEventData;
import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.models.entities.MailTemplate;
import pl.dgrecki.models.enums.EventType;
import pl.dgrecki.services.MailService;
import pl.dgrecki.services.MailTemplateService;

@ExtendWith(MockitoExtension.class)
class UserRegistrationHandlerUnitTests {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MailTemplateService mailTemplateService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private UserRegistrationHandler handler;

    @Test
    void shouldReturnCorrectEventType() {
        assertEquals(EventType.USER_REGISTRATION, handler.getSupportedEventType());
    }

    @Test
    void shouldHandleEventAndSendEmail() {
        InboxEvent inboxEvent = new InboxEvent();

        JsonNode dataNode = mock(JsonNode.class);
        inboxEvent.setData(dataNode);

        String activationLink = "https://example.com";
        String userMail = "test@example.com";
        String userName = "John";
        String subject = "Welcome!";
        String templateName = "user-registration";

        UserRegistrationEventData eventData = new UserRegistrationEventData(userMail, userName, activationLink);

        MailTemplate template = MailTemplate.builder()
                .templateName(templateName)
                .templateHtml("<p>Hello</p>")
                .subject(subject)
                .templateKeys(Set.of("firstName", "activationLink"))
                .build();

        when(objectMapper.convertValue(dataNode, UserRegistrationEventData.class))
                .thenReturn(eventData);

        when(mailTemplateService.getUserRegistrationMailTemplate()).thenReturn(template);

        handler.handle(inboxEvent);

        ArgumentCaptor<MailData> mailDataCaptor = ArgumentCaptor.forClass(MailData.class);
        verify(mailTemplateService, times(1)).validateTemplateVariables(eq(template), anyMap());
        verify(mailService, times(1)).send(mailDataCaptor.capture());

        MailData sentData = mailDataCaptor.getValue();
        assertEquals(userMail, sentData.getTo());
        assertEquals(templateName, sentData.getTemplateName());
        assertEquals(subject, sentData.getSubject());
        assertEquals(userName, sentData.getVariables().get("firstName"));
        assertEquals(activationLink, sentData.getVariables().get("activationLink"));
    }
}
