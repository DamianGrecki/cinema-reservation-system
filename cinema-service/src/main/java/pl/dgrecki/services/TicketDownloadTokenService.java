package pl.dgrecki.services;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TicketDownloadTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;

    public TicketDownloadTokenService(@Value("${ticket.download.signing-secret}") String secret) {
        this.secret = secret;
    }

    public String generateUrl(UUID orderId) {
        String signature = sign(orderId);
        return String.format("/api/orders/%s/tickets/download?signature=%s", orderId, signature);
    }

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
