package pl.dgrecki.services.payments;

import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.PaymentProvider.SANDBOX;
import static pl.dgrecki.models.enums.PaymentStatus.PENDING;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.dgrecki.config.AuthenticatedUser;
import pl.dgrecki.constants.SandboxPaymentProviderEndpoints;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.models.entities.Customer;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.enums.*;
import pl.dgrecki.models.external.SandboxPaymentRequest;
import pl.dgrecki.models.external.SandboxPaymentResponse;
import pl.dgrecki.models.requests.CreatePaymentRequest;
import pl.dgrecki.models.responses.SuccessResponse;
import pl.dgrecki.services.CustomerService;
import pl.dgrecki.services.OrderService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SandboxPaymentService implements PaymentProviderService {

    private final OrderService orderService;
    private final PaymentAttemptService paymentAttemptService;
    private final PaymentFailureService paymentFailureService;
    private final RestClient sandboxPaymentProviderRestClient;
    private final CustomerService customerService;

    @Override
    public PaymentProvider getProviderName() {
        return SANDBOX;
    }

    @Override
    public SuccessResponse createPayment(CreatePaymentRequest request) {
        UUID basketId = request.basketId();

        UUID customerId = resolveCustomerId();
        validateGuestDataIfNotAuthenticated(customerId, request.guestEmail(), request.guestFirstName());
        validatePaymentsIfAlreadyExists(basketId);

        Order order = orderService.prepareOrder(
                customerId, request.guestEmail(), request.guestFirstName(), basketId, getProviderName());

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
            response = sandboxPaymentProviderRestClient
                    .post()
                    .uri(SandboxPaymentProviderEndpoints.PAYMENT_ENDPOINT)
                    .body(body)
                    .retrieve()
                    .body(SandboxPaymentResponse.class);
        } catch (Exception e) {
            log.error("Cannot send request for create payment to Sandbox Payment Provider", e);
            paymentFailureService.failPaymentAttempt(order, null, e.getMessage());
            throw new PaymentProcessException(PROVIDER_CANNOT_CREATE_PAYMENT_MSG);
        }
        handleProviderResponse(response, order);
        return response;
    }

    private UUID resolveCustomerId() {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            return null;
        }
        Customer customer = customerService.getOrFetchCustomer(authenticatedUser.getUserId());
        return customer.getId();
    }

    private void validateGuestDataIfNotAuthenticated(UUID customerId, String guestEmail, String guestFirstName) {
        if (customerId != null) {
            return;
        }
        if (guestEmail == null || guestEmail.isBlank() || guestFirstName == null || guestFirstName.isBlank()) {
            throw new PaymentProcessException(CUSTOMER_DATA_REQUIRED_MSG);
        }
    }

    private AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        return null;
    }

    private void validatePaymentsIfAlreadyExists(UUID basketId) {
        orderService.findExistingOrderByBasketId(basketId).ifPresent(order -> {
            paymentFailureService.stopNewPaymentIfPendingAttemptExists(order);
            paymentFailureService.failPaymentAfterRetryLimit(order);
        });
    }

    private void handleProviderResponse(SandboxPaymentResponse response, Order order) {
        if (Objects.isNull(response)) {
            log.error("Empty response from Sandbox Payment Provider, payment failed");
            paymentFailureService.failPaymentAttempt(order, null, "EmptyResponse");
            throw new PaymentProcessException(PROVIDER_NO_RESPONSE_MSG);
        }

        if (!response.status().equals(PaymentStatus.PENDING)) {
            log.error("Bad transaction status from Sandbox Payment Provider, payment failed");
            paymentFailureService.failPaymentAttempt(
                    order,
                    response.transactionId().toString(),
                    String.format("Expected status %s, but got %s", PENDING, response.status()));
            throw new PaymentProcessException(PROVIDER_BAD_PAYMENT_STATUS_MSG);
        }
    }
}
