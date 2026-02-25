package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.PAYMENT_ATTEMPTS_LIMIT_REACHED_MSG;
import static pl.dgrecki.constants.ExceptionMessages.PAYMENT_ATTEMPT_ALREADY_EXISTS_MSG;
import static pl.dgrecki.models.enums.PaymentStatus.FAILED;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.services.ReservationService;
import pl.dgrecki.services.payments.PaymentAttemptService;
import pl.dgrecki.services.payments.PaymentFailureService;

@ExtendWith(MockitoExtension.class)
class PaymentFailureServiceUnitTests {

    @Mock
    private PaymentAttemptService paymentAttemptService;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private PaymentFailureService paymentFailureService;

    @Test
    void failPaymentAttemptWhenLimitNotReachedShouldSetPaymentAttemptFailedReservationStatusTest() {
        Order order = Order.builder().id(UUID.randomUUID()).build();
        String errorMessage = "Connection refused";

        when(paymentAttemptService.isAttemptsLimitReached(order)).thenReturn(false);

        paymentFailureService.failPaymentAttempt(order, null, errorMessage);

        verify(paymentAttemptService, times(1)).createAttempt(order, null, FAILED, errorMessage);
        verify(reservationService, times(1)).setPaymentAttemptFailedForReservationsByOrder(order);
        verify(reservationService, never()).setPaymentFailedForReservationsByOrder(any());
    }

    @Test
    void failPaymentAttemptWhenLimitReachedShouldSetPaymentFailedStatusTest() {
        Order order = Order.builder().id(UUID.randomUUID()).build();
        String transactionId = UUID.randomUUID().toString();
        String errorMessage = "Connection refused";

        when(paymentAttemptService.isAttemptsLimitReached(order)).thenReturn(true);

        paymentFailureService.failPaymentAttempt(order, transactionId, errorMessage);

        verify(paymentAttemptService, times(1)).createAttempt(order, transactionId, FAILED, errorMessage);
        verify(reservationService, times(1)).setPaymentFailedForReservationsByOrder(order);
        verify(reservationService, never()).setPaymentAttemptFailedForReservationsByOrder(any());
    }

    @Test
    void failPaymentAfterRetryLimitWhenLimitNotReachedShouldDoNothingTest() {
        Order order = Order.builder().id(UUID.randomUUID()).build();

        when(paymentAttemptService.isAttemptsLimitReached(order)).thenReturn(false);

        assertDoesNotThrow(() -> paymentFailureService.failPaymentAfterRetryLimit(order));

        verify(reservationService, never()).setPaymentFailedForReservationsByOrder(any());
    }

    @Test
    void failPaymentAfterRetryLimitWhenLimitReachedShouldSetFailedStatusAndThrowTest() {
        Order order = Order.builder().id(UUID.randomUUID()).build();

        when(paymentAttemptService.isAttemptsLimitReached(order)).thenReturn(true);

        PaymentProcessException ex = assertThrows(
                PaymentProcessException.class, () -> paymentFailureService.failPaymentAfterRetryLimit(order));

        assertEquals(PAYMENT_ATTEMPTS_LIMIT_REACHED_MSG, ex.getMessage());
        verify(reservationService, times(1)).setPaymentFailedForReservationsByOrder(order);
    }

    @Test
    void stopNewPaymentIfPendingAttemptExistsWhenPendingAttemptExistsShouldThrowTest() {
        Order order = Order.builder().id(UUID.randomUUID()).build();

        when(paymentAttemptService.hasPendingPaymentAttempt(order)).thenReturn(true);

        PaymentProcessException ex = assertThrows(
                PaymentProcessException.class, () -> paymentFailureService.stopNewPaymentIfPendingAttemptExists(order));

        assertEquals(PAYMENT_ATTEMPT_ALREADY_EXISTS_MSG, ex.getMessage());
    }

    @Test
    void stopNewPaymentIfPendingAttemptExistsWhenNoPendingAttemptShouldDoNothingTest() {
        Order order = Order.builder().id(UUID.randomUUID()).build();

        when(paymentAttemptService.hasPendingPaymentAttempt(order)).thenReturn(false);

        assertDoesNotThrow(() -> paymentFailureService.stopNewPaymentIfPendingAttemptExists(order));
    }
}
