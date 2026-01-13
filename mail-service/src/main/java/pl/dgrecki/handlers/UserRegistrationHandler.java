package pl.dgrecki.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.MailData;
import pl.dgrecki.models.UserRegistrationEventData;
import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.models.entities.MailTemplate;
import pl.dgrecki.models.enums.EventType;
import pl.dgrecki.services.MailService;
import pl.dgrecki.services.MailTemplateService;

@Component
@RequiredArgsConstructor
public class UserRegistrationHandler implements InboxEventHandler {

    private final ObjectMapper objectMapper;
    private final MailTemplateService mailTemplateService;
    private final MailService mailService;

    @Override
    public EventType getSupportedEventType() {
        return EventType.USER_REGISTRATION;
    }

    @Override
    public void handle(InboxEvent event) {
        UserRegistrationEventData data = objectMapper.convertValue(event.getData(), UserRegistrationEventData.class);
        MailTemplate mailTemplate = mailTemplateService.getUserRegistrationMailTemplate();

        MailData mailData = MailData.builder()
                .to(data.getUserEmail())
                .subject(mailTemplate.getSubject())
                .templateName(mailTemplate.getTemplateName())
                .templateHtml(mailTemplate.getTemplateHtml())
                .variables(Map.of(
                        "firstName", data.getUserFirstName(),
                        "activationLink", data.getActivationLink()))
                .build();
        mailTemplateService.validateTemplateVariables(mailTemplate, mailData.getVariables());
        mailService.send(mailData);
    }
}
