package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.PaymentStatus.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.enums.Currency;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.external.SandboxPaymentResponse;
import pl.dgrecki.models.requests.CreatePaymentRequest;
import pl.dgrecki.services.OrderService;
import pl.dgrecki.services.payments.PaymentAttemptService;
import pl.dgrecki.services.payments.PaymentFailureService;
import pl.dgrecki.services.payments.SandboxPaymentService;

@ExtendWith(MockitoExtension.class)
class SandboxPaymentServiceUnitTests {

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentAttemptService paymentAttemptService;

    @Mock
    private PaymentFailureService paymentFailureService;

    @Mock
    private RestClient sandboxPaymentProviderRestClient;

    @Mock(answer = Answers.RETURNS_SELF)
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private SandboxPaymentService sandboxPaymentService;

    @BeforeEach
    void setUp() {
        lenient().when(sandboxPaymentProviderRestClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
    }

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
        when(responseSpec.body(SandboxPaymentResponse.class)).thenReturn(providerResponse);

        sandboxPaymentService.createPayment(request);

        verify(paymentAttemptService).createAttempt(order, transactionId.toString(), PENDING, null);
    }

    @Test
    void createPaymentWhenAttemptsLimitReachedShouldThrowTest() {
        UUID customerId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(basketId, PaymentProvider.SANDBOX, customerId, null);

        Order existingOrder = Order.builder().id(UUID.randomUUID()).build();

        when(orderService.findExistingOrderByBasketId(basketId)).thenReturn(Optional.of(existingOrder));
        doThrow(new PaymentProcessException(PAYMENT_ATTEMPTS_LIMIT_REACHED_MSG))
                .when(paymentFailureService)
                .failPaymentAfterRetryLimit(existingOrder);

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(PAYMENT_ATTEMPTS_LIMIT_REACHED_MSG, ex.getMessage());
        verify(orderService, never()).prepareOrder(any(), any(), any(), any());
        verify(paymentAttemptService, never()).createAttempt(any(), any(), any(), any());
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
        when(responseSpec.body(SandboxPaymentResponse.class)).thenThrow(networkError);

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(PROVIDER_CANNOT_CREATE_PAYMENT_MSG, ex.getMessage());
        verify(paymentFailureService).failPaymentAttempt(eq(order), isNull(), eq(networkError.getMessage()));
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

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(PROVIDER_NO_RESPONSE_MSG, ex.getMessage());
        verify(paymentFailureService).failPaymentAttempt(eq(order), isNull(), anyString());
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
        when(responseSpec.body(SandboxPaymentResponse.class)).thenReturn(badStatusResponse);

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(PROVIDER_BAD_PAYMENT_STATUS_MSG, ex.getMessage());
        verify(paymentFailureService).failPaymentAttempt(eq(order), eq(transactionId.toString()), anyString());
    }
}
