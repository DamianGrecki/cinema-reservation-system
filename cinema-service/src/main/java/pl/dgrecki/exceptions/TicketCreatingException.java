package pl.dgrecki.exceptions;

public class TicketCreatingException extends RuntimeException {
    public TicketCreatingException(String message) {
        super(message);
    }
}
