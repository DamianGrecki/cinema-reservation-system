package org.example.models.responses;

import lombok.Getter;

@Getter
public class UserRegisterResponse {

    private final boolean isSuccess;
    private final String email;

    public UserRegisterResponse(boolean isSuccess, String email) {
        this.isSuccess = isSuccess;
        this.email = email;
    }
}
