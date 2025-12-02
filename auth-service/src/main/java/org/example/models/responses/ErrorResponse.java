package org.example.models.responses;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final String message;
    private final boolean isSuccess = false;

    public ErrorResponse(String message) {
        this.message = message;
    }
}
