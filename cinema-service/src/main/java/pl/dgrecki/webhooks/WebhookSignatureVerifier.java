package pl.dgrecki.webhooks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.dgrecki.exceptions.WebhookAuthenticationException;
import pl.dgrecki.kafka.HmacSigner;

@Component
public class WebhookSignatureVerifier {

    static final String SIGNATURE_HEADER = "X-Signature";

    private final HmacSigner hmacSigner;

    public WebhookSignatureVerifier(@Value("${payment-provider.sandbox.hmac-secret}") String secret) {
        this.hmacSigner = new HmacSigner(secret);
    }

    public void verify(String signature, String rawPayload) {
        if (signature == null || !hmacSigner.verify(rawPayload, signature)) {
            throw new WebhookAuthenticationException("Invalid or missing webhook signature");
        }
    }
}
