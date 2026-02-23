package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.PaymentStatus.*;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.enums.Currency;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.external.SandboxPaymentResponse;
import pl.dgrecki.models.requests.CreatePaymentRequest;
import pl.dgrecki.services.OrderService;
import pl.dgrecki.services.payments.PaymentAttemptService;
import pl.dgrecki.services.payments.SandboxPaymentService;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class SandboxPaymentServiceUnitTests {

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentAttemptService paymentAttemptService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient sandboxPaymentProviderWebClient;

    @InjectMocks
    private SandboxPaymentService sandboxPaymentService;

    @Test
    void createPaymentTest() {
        UUID customerId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(basketId, PaymentProvider.SANDBOX, customerId, null);

        Order order = Order.builder()
                .id(orderId)
                .customerId(customerId)
                .price(new BigDecimal("25.00"))
                .currency(Currency.PLN)
                .provider(PaymentProvider.SANDBOX)
                .build();

        SandboxPaymentResponse providerResponse = new SandboxPaymentResponse(transactionId, orderId, PENDING, null);

        when(orderService.prepareOrder(customerId, null, basketId, PaymentProvider.SANDBOX))
                .thenReturn(order);
        when(sandboxPaymentProviderWebClient
                        .post()
                        .uri(anyString())
                        .bodyValue(any())
                        .retrieve()
                        .bodyToMono(SandboxPaymentResponse.class))
                .thenReturn(Mono.just(providerResponse));

        sandboxPaymentService.createPayment(request);

        verify(paymentAttemptService, times(1)).createAttempt(order, transactionId.toString(), PENDING, null);
    }

    @Test
    void createPaymentWhenCustomerDataNullShouldThrowTest() {
        CreatePaymentRequest request = new CreatePaymentRequest(UUID.randomUUID(), PaymentProvider.SANDBOX, null, null);

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(CUSTOMER_DATA_REQUIRED_MSG, ex.getMessage());
        verify(orderService, never()).prepareOrder(any(), any(), any(), any());
        verify(paymentAttemptService, never()).createAttempt(any(), any(), any(), any());
    }

    @Test
    void createPaymentWhenHttpCallFailsShouldSaveFailedAttemptAndThrowTest() {
        UUID customerId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(basketId, PaymentProvider.SANDBOX, customerId, null);

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("25.00"))
                .currency(Currency.PLN)
                .build();

        RuntimeException networkError = new RuntimeException("Connection refused");

        when(orderService.prepareOrder(customerId, null, basketId, PaymentProvider.SANDBOX))
                .thenReturn(order);
        when(sandboxPaymentProviderWebClient
                        .post()
                        .uri(anyString())
                        .bodyValue(any())
                        .retrieve()
                        .bodyToMono(SandboxPaymentResponse.class))
                .thenReturn(Mono.error(networkError));

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(PROVIDER_CANNOT_CREATE_PAYMENT_MSG, ex.getMessage());
        verify(paymentAttemptService, times(1))
                .createAttempt(eq(order), isNull(), eq(FAILED), eq(networkError.getMessage()));
    }

    @Test
    void createPaymentWhenResponseIsNullShouldSaveFailedAttemptAndThrowTest() {
        UUID customerId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(basketId, PaymentProvider.SANDBOX, customerId, null);

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("25.00"))
                .currency(Currency.PLN)
                .build();

        when(orderService.prepareOrder(customerId, null, basketId, PaymentProvider.SANDBOX))
                .thenReturn(order);
        when(sandboxPaymentProviderWebClient
                        .post()
                        .uri(anyString())
                        .bodyValue(any())
                        .retrieve()
                        .bodyToMono(SandboxPaymentResponse.class))
                .thenReturn(Mono.empty());

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(PROVIDER_NO_RESPONSE_MSG, ex.getMessage());
        verify(paymentAttemptService, times(1)).createAttempt(eq(order), isNull(), eq(FAILED), any());
    }

    @Test
    void createPaymentWhenResponseStatusNotPendingShouldSaveFailedAttemptAndThrowTest() {
        UUID customerId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(basketId, PaymentProvider.SANDBOX, customerId, null);

        Order order = Order.builder()
                .id(orderId)
                .price(new BigDecimal("25.00"))
                .currency(Currency.PLN)
                .build();

        SandboxPaymentResponse badStatusResponse = new SandboxPaymentResponse(transactionId, orderId, COMPLETED, null);

        when(orderService.prepareOrder(customerId, null, basketId, PaymentProvider.SANDBOX))
                .thenReturn(order);
        when(sandboxPaymentProviderWebClient
                        .post()
                        .uri(anyString())
                        .bodyValue(any())
                        .retrieve()
                        .bodyToMono(SandboxPaymentResponse.class))
                .thenReturn(Mono.just(badStatusResponse));

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(PROVIDER_BAD_PAYMENT_STATUS_MSG, ex.getMessage());
        verify(paymentAttemptService, times(1))
                .createAttempt(eq(order), eq(transactionId.toString()), eq(FAILED), any());
    }
}
