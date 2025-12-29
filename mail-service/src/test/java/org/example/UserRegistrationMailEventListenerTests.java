package org.example;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.example.listeners.UserRegistrationMailEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
class UserRegistrationMailEventListenerTests {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    private UserRegistrationMailEventListener listener;

    @BeforeEach
    void setUp() {
        String emailFrom = "sender@example.com";
        ObjectMapper objectMapper = new ObjectMapper();
        listener = new UserRegistrationMailEventListener(mailSender, templateEngine, objectMapper, emailFrom);
    }

    @Test
    void shouldSendEmailOnValidEventTest() {
        String json = """
                {
                    "eventType":"USER_REGISTRATION_MAIL",
                    "template":"mail_template_v1",
                    "to":"customer8@example.com",
                    "subject":"Welcome in Cinema Service!",
                    "data":{
                        "userName":"Test",
                        "activationLink":"https://example.com/"
                    }
                }
                """;
        String template = "mail_template_v1";

        when(templateEngine.process(eq(template), any())).thenReturn("<html><body>Email body</body></html>");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        listener.handleUserRegistrationMailEvent(json);

        verify(templateEngine, times(1)).process(eq(template), any());
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void shouldThrowExceptionOnInvalidJsonTest() {
        String invalidJson = "invalid-json";

        assertThrows(Exception.class, () -> listener.handleUserRegistrationMailEvent(invalidJson));

        verifyNoInteractions(mailSender);
        verifyNoInteractions(templateEngine);
    }
}
