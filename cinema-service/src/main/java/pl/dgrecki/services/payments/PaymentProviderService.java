package pl.dgrecki.services.payments;

import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.requests.CreatePaymentRequest;
import pl.dgrecki.models.responses.SuccessResponse;

public interface PaymentProviderService {

    PaymentProvider getProviderName();

    SuccessResponse createPayment(CreatePaymentRequest request);
}
