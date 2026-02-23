package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.enums.PaymentStatus;
import pl.dgrecki.repositories.PaymentAttemptRepository;
import pl.dgrecki.services.payments.PaymentAttemptService;

@ExtendWith(MockitoExtension.class)
class PaymentAttemptServiceUnitTests {

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private PaymentAttemptService paymentAttemptService;

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void createAttemptTest() {
        Order order = Order.builder().id(UUID.randomUUID()).build();
        String transactionId = UUID.randomUUID().toString();

        when(clock.instant()).thenReturn(FIXED_NOW);

        paymentAttemptService.createAttempt(order, transactionId, PaymentStatus.PENDING, null);

        ArgumentCaptor<PaymentAttempt> captor = ArgumentCaptor.forClass(PaymentAttempt.class);
        verify(paymentAttemptRepository, times(1)).save(captor.capture());

        PaymentAttempt saved = captor.getValue();
        assertEquals(order, saved.getOrder());
        assertEquals(transactionId, saved.getTransactionId());
        assertEquals(PaymentStatus.PENDING, saved.getStatus());
        assertNull(saved.getProviderError());
        assertEquals(FIXED_NOW, saved.getCreatedAt());
    }

    @Test
    void createAttemptWithProviderErrorTest() {
        Order order = Order.builder().id(UUID.randomUUID()).build();
        String errorMessage = "Connection refused";

        when(clock.instant()).thenReturn(FIXED_NOW);

        paymentAttemptService.createAttempt(order, null, PaymentStatus.FAILED, errorMessage);

        ArgumentCaptor<PaymentAttempt> captor = ArgumentCaptor.forClass(PaymentAttempt.class);
        verify(paymentAttemptRepository, times(1)).save(captor.capture());

        PaymentAttempt saved = captor.getValue();
        assertEquals(PaymentStatus.FAILED, saved.getStatus());
        assertNull(saved.getTransactionId());
        assertEquals(errorMessage, saved.getProviderError());
        assertEquals(FIXED_NOW, saved.getCreatedAt());
    }
}
