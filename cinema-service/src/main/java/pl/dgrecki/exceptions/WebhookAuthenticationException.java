package pl.dgrecki.exceptions;

public class WebhookAuthenticationException extends RuntimeException {
    public WebhookAuthenticationException(String message) {
        super(message);
    }
}
