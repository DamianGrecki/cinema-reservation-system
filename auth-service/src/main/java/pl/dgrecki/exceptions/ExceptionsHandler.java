package pl.dgrecki.exceptions;

import static pl.dgrecki.constants.ExceptionMessages.INCORRECT_CREDENTIALS_MSG;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.dgrecki.models.responses.ErrorResponse;
import pl.dgrecki.models.responses.ValidationErrorsResponse;

@RestControllerAdvice
@Slf4j
public class ExceptionsHandler {

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

    @ExceptionHandler({BadCredentialsException.class, InternalAuthenticationServiceException.class})
    public ResponseEntity<ErrorResponse> handleAuthErrors(AuthenticationException ex) {
        log.warn("Bad credentials exception occurred", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(INCORRECT_CREDENTIALS_MSG));
    }

    @ExceptionHandler(ActivationTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleActivationTokenExpiredError(ActivationTokenExpiredException ex) {
        log.warn("Activation account token has expired", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ActivationTokenIsUsedException.class)
    public ResponseEntity<ErrorResponse> handleActivationTokenExpiredError(ActivationTokenIsUsedException ex) {
        log.warn("Activation account token has already been used", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }
}
