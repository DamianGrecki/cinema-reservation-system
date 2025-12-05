package org.example.models.responses;

import java.util.List;
import lombok.Getter;

@Getter
public class ValidationErrorsResponse {
    private final List<String> errors;
    private final boolean isSuccess = false;

    public ValidationErrorsResponse(List<String> errors) {
        this.errors = errors;
    }
}
