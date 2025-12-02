package org.example.models.responses;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationErrorsResponse {
    private final List<String> errors;
    private final boolean isSuccess = false;

    public ValidationErrorsResponse(List<String> errors) {
        this.errors = errors;
    }
}
