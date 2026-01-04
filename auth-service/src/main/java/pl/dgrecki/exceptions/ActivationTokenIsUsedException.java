package pl.dgrecki.exceptions;

public class ActivationTokenIsUsedException extends RuntimeException {
    public ActivationTokenIsUsedException(String message) {
        super(message);
    }
}
