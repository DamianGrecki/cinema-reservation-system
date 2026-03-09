package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.enums.RefundStatus;
import pl.dgrecki.models.external.SandboxRefundResponse;
import pl.dgrecki.services.payments.RefundProviderService.RefundProviderResponse;
import pl.dgrecki.services.payments.SandboxRefundProviderService;

@ExtendWith(MockitoExtension.class)
class SandboxRefundProviderServiceUnitTests {

    @Mock
    private RestClient sandboxPaymentProviderRestClient;

    @Mock(answer = Answers.RETURNS_SELF)
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private SandboxRefundProviderService sandboxRefundProviderService;

    @BeforeEach
    void setUp() {
        sandboxRefundProviderService = new SandboxRefundProviderService(sandboxPaymentProviderRestClient);
        lenient().when(sandboxPaymentProviderRestClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getProviderNameShouldReturnSandboxTest() {
        assertEquals(PaymentProvider.SANDBOX, sandboxRefundProviderService.getProviderName());
    }

    @Test
    void sendRefundTest() {
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        String txId = UUID.randomUUID().toString();

        Order order = Order.builder().id(orderId).build();
        PaymentAttempt attempt = PaymentAttempt.builder().transactionId(txId).build();

        SandboxRefundResponse providerResponse =
                new SandboxRefundResponse(transactionId, orderId, RefundStatus.COMPLETED);

        when(responseSpec.body(SandboxRefundResponse.class)).thenReturn(providerResponse);

        RefundProviderResponse result = sandboxRefundProviderService.sendRefund(attempt, order);

        assertNotNull(result);
        assertEquals(transactionId.toString(), result.transactionId());
        assertEquals(RefundStatus.COMPLETED, result.status());
    }

    @Test
    void sendRefundWhenResponseIsNullShouldReturnNullTest() {
        UUID orderId = UUID.randomUUID();
        String txId = UUID.randomUUID().toString();

        Order order = Order.builder().id(orderId).build();
        PaymentAttempt attempt = PaymentAttempt.builder().transactionId(txId).build();

        when(responseSpec.body(SandboxRefundResponse.class)).thenReturn(null);

        RefundProviderResponse result = sandboxRefundProviderService.sendRefund(attempt, order);

        assertNull(result);
    }

    @Test
    void sendRefundWhenHttpCallFailsShouldReturnNullTest() {
        UUID orderId = UUID.randomUUID();
        String txId = UUID.randomUUID().toString();

        Order order = Order.builder().id(orderId).build();
        PaymentAttempt attempt = PaymentAttempt.builder().transactionId(txId).build();

        when(responseSpec.body(SandboxRefundResponse.class)).thenThrow(new RuntimeException("Connection refused"));

        RefundProviderResponse result = sandboxRefundProviderService.sendRefund(attempt, order);

        assertNull(result);
    }
}
