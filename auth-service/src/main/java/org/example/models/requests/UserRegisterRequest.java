package org.example.models.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisterRequest {

    private final String email;
    private final String password;
    private final String confirmPassword;
}
