package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.PaymentStatus.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;
import pl.dgrecki.config.AuthenticatedUser;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.models.entities.Customer;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.enums.Currency;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.external.SandboxPaymentResponse;
import pl.dgrecki.models.requests.CreatePaymentRequest;
import pl.dgrecki.services.CustomerService;
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

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private SandboxPaymentService sandboxPaymentService;

    @BeforeEach
    void setUp() {
        lenient().when(sandboxPaymentProviderRestClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPaymentForAuthenticatedUserTest() {
        UUID customerId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        Long userId = 1L;

        setAuthenticatedUser(userId, "user@example.com");

        CreatePaymentRequest request = new CreatePaymentRequest(basketId, PaymentProvider.SANDBOX, null, null);

        Customer customer = Customer.builder().id(customerId).userId(userId).build();
        when(customerService.getOrFetchCustomer(userId)).thenReturn(customer);

        Order order = Order.builder()
                .id(orderId)
                .customerId(customerId)
                .price(new BigDecimal("25.00"))
                .currency(Currency.PLN)
                .provider(PaymentProvider.SANDBOX)
                .build();

        SandboxPaymentResponse providerResponse = new SandboxPaymentResponse(transactionId, orderId, PENDING, null);

        when(orderService.prepareOrder(customerId, null, null, basketId, PaymentProvider.SANDBOX))
                .thenReturn(order);
        when(responseSpec.body(SandboxPaymentResponse.class)).thenReturn(providerResponse);

        sandboxPaymentService.createPayment(request);

        verify(customerService).getOrFetchCustomer(userId);
        verify(paymentAttemptService).createAttempt(order, transactionId.toString(), PENDING, null);
    }

    @Test
    void createPaymentForGuestUserTest() {
        UUID basketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        String guestEmail = "guest@example.com";
        String guestFirstName = "Jan";

        CreatePaymentRequest request =
                new CreatePaymentRequest(basketId, PaymentProvider.SANDBOX, guestEmail, guestFirstName);

        Order order = Order.builder()
                .id(orderId)
                .guestEmail(guestEmail)
                .guestFirstName(guestFirstName)
                .price(new BigDecimal("25.00"))
                .currency(Currency.PLN)
                .provider(PaymentProvider.SANDBOX)
                .build();

        SandboxPaymentResponse providerResponse = new SandboxPaymentResponse(transactionId, orderId, PENDING, null);

        when(orderService.prepareOrder(null, guestEmail, guestFirstName, basketId, PaymentProvider.SANDBOX))
                .thenReturn(order);
        when(responseSpec.body(SandboxPaymentResponse.class)).thenReturn(providerResponse);

        sandboxPaymentService.createPayment(request);

        verify(customerService, never()).getOrFetchCustomer(any());
        verify(paymentAttemptService).createAttempt(order, transactionId.toString(), PENDING, null);
    }

    @Test
    void createPaymentWhenGuestDataNullShouldThrowTest() {
        CreatePaymentRequest request = new CreatePaymentRequest(UUID.randomUUID(), PaymentProvider.SANDBOX, null, null);

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(CUSTOMER_DATA_REQUIRED_MSG, ex.getMessage());
        verify(orderService, never()).prepareOrder(any(), any(), any(), any(), any());
        verify(paymentAttemptService, never()).createAttempt(any(), any(), any(), any());
    }

    @Test
    void createPaymentWhenGuestDataBlankShouldThrowTest() {
        CreatePaymentRequest request = new CreatePaymentRequest(UUID.randomUUID(), PaymentProvider.SANDBOX, "  ", "");

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(CUSTOMER_DATA_REQUIRED_MSG, ex.getMessage());
    }

    @Test
    void createPaymentWhenAttemptsLimitReachedShouldThrowTest() {
        UUID basketId = UUID.randomUUID();
        String guestEmail = "guest@example.com";
        String guestFirstName = "Jan";

        CreatePaymentRequest request =
                new CreatePaymentRequest(basketId, PaymentProvider.SANDBOX, guestEmail, guestFirstName);

        Order existingOrder = Order.builder().id(UUID.randomUUID()).build();

        when(orderService.findExistingOrderByBasketId(basketId)).thenReturn(Optional.of(existingOrder));
        doThrow(new PaymentProcessException(PAYMENT_ATTEMPTS_LIMIT_REACHED_MSG))
                .when(paymentFailureService)
                .failPaymentAfterRetryLimit(existingOrder);

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(PAYMENT_ATTEMPTS_LIMIT_REACHED_MSG, ex.getMessage());
        verify(orderService, never()).prepareOrder(any(), any(), any(), any(), any());
        verify(paymentAttemptService, never()).createAttempt(any(), any(), any(), any());
    }

    @Test
    void createPaymentWhenHttpCallFailsShouldSaveFailedAttemptAndThrowTest() {
        UUID basketId = UUID.randomUUID();
        String guestEmail = "guest@example.com";
        String guestFirstName = "Jan";

        CreatePaymentRequest request =
                new CreatePaymentRequest(basketId, PaymentProvider.SANDBOX, guestEmail, guestFirstName);

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("25.00"))
                .currency(Currency.PLN)
                .build();

        RuntimeException networkError = new RuntimeException("Connection refused");

        when(orderService.prepareOrder(null, guestEmail, guestFirstName, basketId, PaymentProvider.SANDBOX))
                .thenReturn(order);
        when(responseSpec.body(SandboxPaymentResponse.class)).thenThrow(networkError);

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(PROVIDER_CANNOT_CREATE_PAYMENT_MSG, ex.getMessage());
        verify(paymentFailureService).failPaymentAttempt(eq(order), isNull(), eq(networkError.getMessage()));
    }

    @Test
    void createPaymentWhenResponseIsNullShouldSaveFailedAttemptAndThrowTest() {
        UUID basketId = UUID.randomUUID();
        String guestEmail = "guest@example.com";
        String guestFirstName = "Jan";

        CreatePaymentRequest request =
                new CreatePaymentRequest(basketId, PaymentProvider.SANDBOX, guestEmail, guestFirstName);

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("25.00"))
                .currency(Currency.PLN)
                .build();

        when(orderService.prepareOrder(null, guestEmail, guestFirstName, basketId, PaymentProvider.SANDBOX))
                .thenReturn(order);

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(PROVIDER_NO_RESPONSE_MSG, ex.getMessage());
        verify(paymentFailureService).failPaymentAttempt(eq(order), isNull(), anyString());
    }

    @Test
    void createPaymentWhenResponseStatusNotPendingShouldSaveFailedAttemptAndThrowTest() {
        UUID basketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        String guestEmail = "guest@example.com";
        String guestFirstName = "Jan";

        CreatePaymentRequest request =
                new CreatePaymentRequest(basketId, PaymentProvider.SANDBOX, guestEmail, guestFirstName);

        Order order = Order.builder()
                .id(orderId)
                .price(new BigDecimal("25.00"))
                .currency(Currency.PLN)
                .build();

        SandboxPaymentResponse badStatusResponse = new SandboxPaymentResponse(transactionId, orderId, COMPLETED, null);

        when(orderService.prepareOrder(null, guestEmail, guestFirstName, basketId, PaymentProvider.SANDBOX))
                .thenReturn(order);
        when(responseSpec.body(SandboxPaymentResponse.class)).thenReturn(badStatusResponse);

        PaymentProcessException ex =
                assertThrows(PaymentProcessException.class, () -> sandboxPaymentService.createPayment(request));

        assertEquals(PROVIDER_BAD_PAYMENT_STATUS_MSG, ex.getMessage());
        verify(paymentFailureService).failPaymentAttempt(eq(order), eq(transactionId.toString()), anyString());
    }

    private void setAuthenticatedUser(Long userId, String email) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, email);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authenticatedUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
