package pl.dgrecki.exceptions;

public class TicketRefundingException extends RuntimeException {
    public TicketRefundingException(String message, Exception exception) {
        super(message, exception);
    }

    public TicketRefundingException(String message) {
        super(message);
    }
}
