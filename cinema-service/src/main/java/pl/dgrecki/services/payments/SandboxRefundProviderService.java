package pl.dgrecki.services.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.dgrecki.constants.SandboxPaymentProviderEndpoints;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.external.SandboxRefundRequest;
import pl.dgrecki.models.external.SandboxRefundResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class SandboxRefundProviderService implements RefundProviderService {

    private final RestClient sandboxPaymentProviderRestClient;

    @Override
    public PaymentProvider getProviderName() {
        return PaymentProvider.SANDBOX;
    }

    @Override
    public RefundProviderResponse sendRefund(PaymentAttempt paymentAttempt, Order order) {
        try {
            SandboxRefundResponse response = sandboxPaymentProviderRestClient
                    .post()
                    .uri(SandboxPaymentProviderEndpoints.REFUND_ENDPOINT)
                    .body(new SandboxRefundRequest(paymentAttempt.getTransactionId(), order.getId()))
                    .retrieve()
                    .body(SandboxRefundResponse.class);

            if (response == null) {
                log.warn("Null response from refund provider for order {}", order.getId());
                return null;
            }

            log.info("Refund provider responded with status {} for order {}", response.status(), order.getId());
            return new RefundProviderResponse(response.transactionId().toString(), response.status());
        } catch (Exception e) {
            log.error("Failed to send refund request for order {}", order.getId(), e);
            return null;
        }
    }
}
