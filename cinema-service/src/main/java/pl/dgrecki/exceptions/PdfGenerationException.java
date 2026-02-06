package pl.dgrecki.exceptions;

public class PdfGenerationException extends RuntimeException {
    public PdfGenerationException(String message, Exception e) {
        super(message, e);
    }
}
