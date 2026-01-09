package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.EVENT_DATA_SERIALIZE_FAILED_MSG;
import static pl.dgrecki.models.enums.EventType.USER_REGISTRATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.events.UserRegistrationEventData;

@Service
@RequiredArgsConstructor
public class EventService {

    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createUserRegistrationEvent(User user, String activationLink) {
        UserRegistrationEventData data =
                new UserRegistrationEventData(user.getEmail(), user.getFirstName(), activationLink);
        String jsonData = toJson(data);
        outboxService.createOutboxEvent(USER_REGISTRATION, jsonData);
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(EVENT_DATA_SERIALIZE_FAILED_MSG, e);
        }
    }
}
