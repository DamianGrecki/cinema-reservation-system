package org.example.exceptions;

import static org.example.constants.ExceptionMessages.INCORRECT_CREDENTIALS_MSG;
import static org.example.constants.ExceptionMessages.UNEXPECTED_ERROR_MSG;

import lombok.extern.slf4j.Slf4j;
import org.example.models.responses.ErrorResponse;
import org.example.models.responses.ValidationErrorsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionsHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(UNEXPECTED_ERROR_MSG));
    }

    @ExceptionHandler(ValidationsException.class)
    public ResponseEntity<ValidationErrorsResponse> handleValidationErrors(ValidationsException ex) {
        log.warn("Validation exceptions occurred: {}, Errors: {}", ex, ex.getMessages());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ValidationErrorsResponse(ex.getMessages()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(ValidationException ex) {
        log.warn("Validation exception occurred", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsError(ResourceAlreadyExistsException ex) {
        log.warn("Resource already exists exception occurred", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundError(ResourceNotFoundException ex) {
        log.warn("Resource not found exception occurred", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials exception occurred", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(INCORRECT_CREDENTIALS_MSG));
    }
}
