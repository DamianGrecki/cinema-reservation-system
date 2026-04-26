package pl.dgrecki.webhooks;

import static pl.dgrecki.constants.Webhooks.PAYMENT_PROVIDER_WEBHOOK;
import static pl.dgrecki.webhooks.WebhookSignatureVerifier.SIGNATURE_HEADER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pl.dgrecki.models.external.SandboxPaymentResponse;
import pl.dgrecki.services.payments.PaymentWebhook;

@RestController
@RequestMapping(PAYMENT_PROVIDER_WEBHOOK)
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentWebhook paymentWebhook;
    private final WebhookSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping
    public ResponseEntity<Void> handlePaymentWebhook(
            @RequestHeader(value = SIGNATURE_HEADER, required = false) String signature,
            @RequestBody String rawPayload) {
        signatureVerifier.verify(signature, rawPayload);
        SandboxPaymentResponse payload = parseAndValidate(rawPayload);
        paymentWebhook.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }

    private SandboxPaymentResponse parseAndValidate(String rawPayload) {
        SandboxPaymentResponse payload;
        try {
            payload = objectMapper.readValue(rawPayload, SandboxPaymentResponse.class);
        } catch (JsonProcessingException _) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook payload");
        }
        Set<ConstraintViolation<SandboxPaymentResponse>> violations = validator.validate(payload);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook payload");
        }
        return payload;
    }
}
