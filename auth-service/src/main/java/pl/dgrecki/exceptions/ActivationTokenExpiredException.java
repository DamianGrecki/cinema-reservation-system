package pl.dgrecki.exceptions;

public class ActivationTokenExpiredException extends RuntimeException {
    public ActivationTokenExpiredException(String message) {
        super(message);
    }
}
