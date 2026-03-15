package pl.dgrecki.exceptions;

public class HmacVerificationException extends RuntimeException {
    public HmacVerificationException(String message) {
        super(message);
    }
}
