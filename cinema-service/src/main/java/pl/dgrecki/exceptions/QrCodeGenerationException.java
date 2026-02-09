package pl.dgrecki.exceptions;

public class QrCodeGenerationException extends RuntimeException {
    public QrCodeGenerationException(String message, Exception e) {
        super(message, e);
    }
}
