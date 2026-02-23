package pl.dgrecki.services.payments;

import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.PaymentProvider.SANDBOX;
import static pl.dgrecki.models.enums.PaymentStatus.FAILED;
import static pl.dgrecki.models.enums.PaymentStatus.PENDING;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.dgrecki.constants.SandboxPaymentProviderEndpoints;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.enums.*;
import pl.dgrecki.models.external.SandboxPaymentRequest;
import pl.dgrecki.models.external.SandboxPaymentResponse;
import pl.dgrecki.models.requests.CreatePaymentRequest;
import pl.dgrecki.models.responses.SuccessResponse;
import pl.dgrecki.services.OrderService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SandboxPaymentService implements PaymentProviderService {

    private final OrderService orderService;
    private final PaymentAttemptService paymentAttemptService;
    private final WebClient sandboxPaymentProviderWebClient;

    @Override
    public PaymentProvider getProviderName() {
        return SANDBOX;
    }

    public SuccessResponse createPayment(CreatePaymentRequest request) {
        UUID customerId = request.customerId();
        String guestEmail = request.guestEmail();
        UUID basketId = request.basketId();

        validateCustomerData(customerId, guestEmail);

        Order order = orderService.prepareOrder(customerId, guestEmail, basketId, getProviderName());

        SandboxPaymentResponse sandboxPaymentResponse = sendPaymentRequestToProvider(order);
        paymentAttemptService.createAttempt(
                order,
                sandboxPaymentResponse.transactionId().toString(),
                sandboxPaymentResponse.status(),
                sandboxPaymentResponse.providerError());
        return new SuccessResponse();
    }

    private SandboxPaymentResponse sendPaymentRequestToProvider(Order order) {
        SandboxPaymentRequest body = new SandboxPaymentRequest(order.getId(), order.getPrice(), order.getCurrency());

        SandboxPaymentResponse response;
        try {
            response = sandboxPaymentProviderWebClient
                    .post()
                    .uri(SandboxPaymentProviderEndpoints.PAYMENT_ENDPOINT)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(SandboxPaymentResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Cannot send request for create payment to Sandbox Payment Provider", e);
            paymentAttemptService.createAttempt(order, null, FAILED, e.getMessage());
            throw new PaymentProcessException(PROVIDER_CANNOT_CREATE_PAYMENT_MSG);
        }
        validateProviderResponse(response, order);
        return response;
    }

    private void validateCustomerData(UUID customerId, String guestEmail) {
        if (Objects.isNull(customerId) && Objects.isNull(guestEmail)) {
            throw new PaymentProcessException(CUSTOMER_DATA_REQUIRED_MSG);
        }
    }

    private void validateProviderResponse(SandboxPaymentResponse response, Order order) {
        if (Objects.isNull(response)) {
            log.error("Empty response from Sandbox Payment Provider, payment failed");
            paymentAttemptService.createAttempt(order, null, FAILED, "Empty response");
            throw new PaymentProcessException(PROVIDER_NO_RESPONSE_MSG);
        }

        if (response.status() != (PaymentStatus.PENDING)) {
            log.error("Bad transaction status from Sandbox Payment Provider, payment failed");
            paymentAttemptService.createAttempt(
                    order,
                    response.transactionId().toString(),
                    FAILED,
                    String.format("Expected status %s, but got %s", PENDING, response.status()));
            throw new PaymentProcessException(PROVIDER_BAD_PAYMENT_STATUS_MSG);
        }
    }
}
