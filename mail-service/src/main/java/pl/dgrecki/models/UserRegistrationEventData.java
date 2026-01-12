package pl.dgrecki.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRegistrationEventData {
    private String userEmail;
    private String userFirstName;
    private String activationLink;
}
