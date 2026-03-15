package pl.dgrecki.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketGeneratedHandler implements InboxEventHandler {

    private final ObjectMapper objectMapper;
    private final MailTemplateService mailTemplateService;
    private final MailService mailService;
    private final AttachmentsService attachmentsService;

    @Override
    public EventType getSupportedEventType() {
        return EventType.TICKET_GENERATED;
    }

    @Override
    public void handle(InboxEvent event) {
        TicketGeneratedEventData data = objectMapper.convertValue(event.getData(), TicketGeneratedEventData.class);
        log.info("Processing ticket generated event {} for order {}", event.getId(), data.getOrderId());

        List<Attachment> attachments = attachmentsService.downloadAttachments(URI.create(data.getDownloadUrl()));

        MailTemplate mailTemplate = mailTemplateService.getMailTemplate(TemplateType.TICKET_GENERATED);

        MailData mailData = MailData.builder()
                .to(data.getUserEmail())
                .subject(mailTemplate.getSubject())
                .templateName(mailTemplate.getTemplateName())
                .templateHtml(mailTemplate.getTemplateHtml())
                .attachments(attachments)
                .variables(Map.of("firstName", data.getUserFirstName()))
                .build();
        mailTemplateService.validateTemplateVariables(mailTemplate, mailData.getVariables());
        mailService.send(mailData);
    }
}
