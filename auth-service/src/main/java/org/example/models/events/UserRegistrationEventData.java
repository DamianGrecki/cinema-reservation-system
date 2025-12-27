package org.example.models.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserRegistrationEventData {
    private String userName;
    private String activationLink;
}
