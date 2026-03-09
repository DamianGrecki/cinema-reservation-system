package pl.dgrecki.services.storage;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "ticket.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalTicketDownloadUrlService implements TicketDownloadUrlService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;
    private final String baseUrl;

    public LocalTicketDownloadUrlService(
            @Value("${ticket.download.signing-secret}") String secret,
            @Value("${ticket.download.base-url}") String baseUrl) {
        this.secret = secret;
        this.baseUrl = baseUrl;
    }

    @Override
    public String generateUrl(UUID orderId) {
        String signature = sign(orderId);
        return String.format("%s/api/orders/%s/tickets/download?signature=%s", baseUrl, orderId, signature);
    }

    @Override
    public boolean isValidSignature(UUID orderId, String signature) {
        String expectedSignature = sign(orderId);
        return expectedSignature.equals(signature);
    }

    private String sign(UUID id) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(id.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }
}
