package org.example.services;

import static org.example.constants.ExceptionMessages.EMAIL_EVENT_SERIALIZE_FAILED_MSG;
import static org.example.models.events.OutboxEvent.AggregateType.USER;
import static org.example.models.events.OutboxEvent.EventType.USER_REGISTRATION_MAIL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.models.User;
import org.example.models.events.EmailEvent;
import org.example.models.events.UserRegistrationEventData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;
    private final String template;
    private final String subject;

    public EventService(
            OutboxService outboxService,
            ObjectMapper objectMapper,
            @Value("${mail.user-registration.template}") String template,
            @Value("${mail.user-registration.subject}") String subject) {
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
        this.template = template;
        this.subject = subject;
    }

    @Transactional
    public void createUserRegistrationMailEvent(User user) {
        // TODO Remove userName and acivationLink placeholder
        UserRegistrationEventData data = new UserRegistrationEventData("Test", "https://example.com/");
        String to = user.getEmail();
        EmailEvent<UserRegistrationEventData> emailEvent =
                new EmailEvent<>(USER_REGISTRATION_MAIL, template, to, subject, data);
        String jsonData = toJson(emailEvent);
        outboxService.createOutboxEvent(USER, user.getId(), USER_REGISTRATION_MAIL, jsonData);
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(EMAIL_EVENT_SERIALIZE_FAILED_MSG, e);
        }
    }
}
