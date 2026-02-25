package pl.dgrecki.webhooks;

import static pl.dgrecki.constants.Webhooks.PAYMENT_PROVIDER_WEBHOOK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.external.SandboxPaymentResponse;
import pl.dgrecki.services.payments.PaymentWebhook;

@RestController
@RequestMapping(PAYMENT_PROVIDER_WEBHOOK)
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentWebhook paymentWebhook;

    @PostMapping
    public ResponseEntity<Void> handlePaymentWebhook(@Valid @RequestBody SandboxPaymentResponse payload) {
        paymentWebhook.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
