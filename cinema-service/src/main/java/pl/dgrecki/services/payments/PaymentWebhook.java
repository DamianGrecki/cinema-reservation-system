package pl.dgrecki.services.payments;

import pl.dgrecki.models.external.SandboxPaymentResponse;

public interface PaymentWebhook {
    void handleWebhook(SandboxPaymentResponse payload);
}
