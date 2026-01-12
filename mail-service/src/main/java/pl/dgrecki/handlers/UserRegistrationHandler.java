package pl.dgrecki.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.MailData;
import pl.dgrecki.models.UserRegistrationEventData;
import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.models.enums.EventType;
import pl.dgrecki.services.MailService;

@Component
@RequiredArgsConstructor
public class UserRegistrationHandler implements InboxEventHandler {

    private final ObjectMapper objectMapper;
    private final MailService mailService;

    @Override
    public EventType getSupportedEventType() {
        return EventType.USER_REGISTRATION;
    }

    @Override
    public void handle(InboxEvent event) {
        UserRegistrationEventData data = objectMapper.convertValue(event.getData(), UserRegistrationEventData.class);

        MailData mailData = MailData.builder()
                .to(data.getUserEmail())
                .subject("Welcome in Cinema Service!")
                .template("user_registration_v1")
                .variables(Map.of(
                        "firstName", data.getUserFirstName(),
                        "activationLink", data.getActivationLink()))
                .build();
        mailService.send(mailData);
    }
}
