package pl.dgrecki.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.dgrecki.models.responses.ErrorResponse;

@RestControllerAdvice
@Slf4j
public class ExceptionsHandler {

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsError(ResourceAlreadyExistsException ex) {
        log.warn("Resource already exists exception occurred", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundError(ResourceNotFoundException ex) {
        log.warn("Resource not found exception occurred", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ReservationProcessException.class)
    public ResponseEntity<ErrorResponse> reservationProcessError(ReservationProcessException ex) {
        log.warn("Reservation process exception occurred", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(TicketCreatingException.class)
    public ResponseEntity<ErrorResponse> ticketCreatingError(TicketCreatingException ex) {
        log.warn("Ticket creating exception occurred", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }
}
