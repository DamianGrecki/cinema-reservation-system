package pl.dgrecki.services.payments;

import static pl.dgrecki.constants.ExceptionMessages.*;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.models.requests.CreatePaymentRequest;
import pl.dgrecki.models.responses.SuccessResponse;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final List<PaymentProviderService> paymentProviders;

    public SuccessResponse createPayment(CreatePaymentRequest request) {
        PaymentProviderService provider = paymentProviders.stream()
                .filter(p -> p.getProviderName().equals(request.provider()))
                .findFirst()
                .orElseThrow(() -> new PaymentProcessException(PROVIDER_IS_UNKNOWN_MSG));

        return provider.createPayment(request);
    }
}
