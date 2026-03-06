package pl.dgrecki.unit.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.handlers.TicketGeneratedHandler;
import pl.dgrecki.models.Attachment;
import pl.dgrecki.models.MailData;
import pl.dgrecki.models.TicketGeneratedEventData;
import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.models.entities.MailTemplate;
import pl.dgrecki.models.enums.EventType;
import pl.dgrecki.models.enums.TemplateType;
import pl.dgrecki.services.AttachmentsService;
import pl.dgrecki.services.MailService;
import pl.dgrecki.services.MailTemplateService;

@ExtendWith(MockitoExtension.class)
class TicketGeneratedHandlerUnitTests {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MailTemplateService mailTemplateService;

    @Mock
    private MailService mailService;

    @Mock
    private AttachmentsService attachmentsService;

    @InjectMocks
    private TicketGeneratedHandler handler;

    @Test
    void shouldReturnCorrectEventType() {
        assertEquals(EventType.TICKET_GENERATED, handler.getSupportedEventType());
    }

    @Test
    void shouldHandleEventAndSendEmailWithAttachments() {
        InboxEvent inboxEvent = new InboxEvent();
        JsonNode dataNode = mock(JsonNode.class);
        inboxEvent.setData(dataNode);

        String userEmail = "test@example.com";
        String userFirstName = "Jan";
        UUID orderId = UUID.randomUUID();
        String downloadUrl = "http://localhost:8080/api/orders/" + orderId + "/tickets/download?signature=abc";
        String subject = "Twoje bilety!";
        String templateName = "ticket_generated_v1";

        TicketGeneratedEventData eventData =
                new TicketGeneratedEventData(userEmail, userFirstName, orderId, downloadUrl);

        MailTemplate template = MailTemplate.builder()
                .templateName(templateName)
                .templateHtml("<p>Bilety</p>")
                .subject(subject)
                .templateKeys(Set.of("firstName"))
                .build();

        List<Attachment> attachments = List.of(
                new Attachment("ticket_1.pdf", "application/pdf", new byte[] {1, 2, 3}),
                new Attachment("ticket_2.pdf", "application/pdf", new byte[] {4, 5, 6}));

        when(objectMapper.convertValue(dataNode, TicketGeneratedEventData.class))
                .thenReturn(eventData);
        when(attachmentsService.downloadAttachments(URI.create(downloadUrl))).thenReturn(attachments);
        when(mailTemplateService.getMailTemplate(TemplateType.TICKET_GENERATED)).thenReturn(template);

        handler.handle(inboxEvent);

        ArgumentCaptor<MailData> mailDataCaptor = ArgumentCaptor.forClass(MailData.class);
        verify(mailTemplateService, times(1)).validateTemplateVariables(eq(template), anyMap());
        verify(mailService, times(1)).send(mailDataCaptor.capture());

        MailData sentData = mailDataCaptor.getValue();
        assertEquals(userEmail, sentData.getTo());
        assertEquals(templateName, sentData.getTemplateName());
        assertEquals(subject, sentData.getSubject());
        assertEquals(userFirstName, sentData.getVariables().get("firstName"));
        assertEquals(attachments, sentData.getAttachments());
        assertEquals(2, sentData.getAttachments().size());
    }
}
