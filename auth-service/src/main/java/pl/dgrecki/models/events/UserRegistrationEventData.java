package pl.dgrecki.models.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserRegistrationEventData {
    private String userEmail;
    private String userFirstName;
    private String activationLink;
}
