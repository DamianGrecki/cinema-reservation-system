package pl.dgrecki.services.payments;

import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.enums.RefundStatus;

public interface RefundProviderService {

    PaymentProvider getProviderName();

    RefundProviderResponse sendRefund(PaymentAttempt paymentAttempt, Order order);

    record RefundProviderResponse(String transactionId, RefundStatus status) {}
}
