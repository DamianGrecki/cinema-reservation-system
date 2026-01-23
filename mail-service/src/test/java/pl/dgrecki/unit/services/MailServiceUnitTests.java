package pl.dgrecki.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import pl.dgrecki.models.MailData;
import pl.dgrecki.services.MailService;

@ExtendWith(MockitoExtension.class)
class MailServiceUnitTests {

    @Mock
    private JavaMailSender mailSender;

    private MailService mailService;

    @BeforeEach
    void setUp() {
        String emailFrom = "no-reply@example.com";
        mailService = new MailService(mailSender, emailFrom);
    }

    @Test
    void shouldSendEmailSuccessfullyTest() {
        String to = "user@example.com";
        String subject = "Welcome!";
        String templateHtml = "<p>Hello, <span th:text=\"${name}\"></span></p>";
        String templateName = "welcome-template";

        MailData mailData = MailData.builder()
                .to(to)
                .subject(subject)
                .templateHtml(templateHtml)
                .templateName(templateName)
                .variables(Map.of("name", "John"))
                .build();

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailService.send(mailData);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        assertSame(mimeMessage, sentMessage);
    }

    @Test
    void shouldPropagateExceptionWhenMailSenderFailsTest() {
        String errorMessage = "SMTP failure";
        String to = "user@example.com";
        MailData mailData = MailData.builder()
                .to(to)
                .subject("Subject")
                .templateHtml("<p>Hello</p>")
                .templateName("template")
                .variables(Map.of())
                .build();

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException(errorMessage)).when(mailSender).send(mimeMessage);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mailService.send(mailData));
        assertEquals(errorMessage, ex.getMessage());
    }
}
