package org.example.models.requests;

import lombok.Getter;

@Getter
public class UserRegisterRequestBody {

    private final String email;
    private final String password;
    private final String confirmPassword;

    public UserRegisterRequestBody(String email, String password, String confirmPassword) {
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
}
