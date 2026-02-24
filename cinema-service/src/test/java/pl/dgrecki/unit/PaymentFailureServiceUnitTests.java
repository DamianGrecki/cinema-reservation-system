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
        UUID basketId = UUID.randomUUID();
        Order order = Order.builder().id(UUID.randomUUID()).build();
        String errorMessage = "Connection refused";

        when(paymentAttemptService.isAttemptsLimitReached(order)).thenReturn(false);

        paymentFailureService.failPaymentAttempt(order, null, basketId, errorMessage);

        verify(paymentAttemptService, times(1)).createAttempt(order, null, FAILED, errorMessage);
        verify(reservationService, times(1)).setPaymentAttemptFailedForReservationsInBasket(basketId);
        verify(reservationService, never()).setPaymentFailedForReservationsInBasket(any());
    }

    @Test
    void failPaymentAttemptWhenLimitReachedShouldSetPaymentFailedStatusTest() {
        UUID basketId = UUID.randomUUID();
        Order order = Order.builder().id(UUID.randomUUID()).build();
        String transactionId = UUID.randomUUID().toString();
        String errorMessage = "Connection refused";

        when(paymentAttemptService.isAttemptsLimitReached(order)).thenReturn(true);

        paymentFailureService.failPaymentAttempt(order, transactionId, basketId, errorMessage);

        verify(paymentAttemptService, times(1)).createAttempt(order, transactionId, FAILED, errorMessage);
        verify(reservationService, times(1)).setPaymentFailedForReservationsInBasket(basketId);
        verify(reservationService, never()).setPaymentAttemptFailedForReservationsInBasket(any());
    }

    @Test
    void failPaymentAfterRetryLimitWhenLimitNotReachedShouldDoNothingTest() {
        UUID basketId = UUID.randomUUID();
        Order order = Order.builder().id(UUID.randomUUID()).build();

        when(paymentAttemptService.isAttemptsLimitReached(order)).thenReturn(false);

        assertDoesNotThrow(() -> paymentFailureService.failPaymentAfterRetryLimit(order, basketId));

        verify(reservationService, never()).setPaymentFailedForReservationsInBasket(any());
    }

    @Test
    void failPaymentAfterRetryLimitWhenLimitReachedShouldSetFailedStatusAndThrowTest() {
        UUID basketId = UUID.randomUUID();
        Order order = Order.builder().id(UUID.randomUUID()).build();

        when(paymentAttemptService.isAttemptsLimitReached(order)).thenReturn(true);

        PaymentProcessException ex = assertThrows(
                PaymentProcessException.class, () -> paymentFailureService.failPaymentAfterRetryLimit(order, basketId));

        assertEquals(PAYMENT_ATTEMPTS_LIMIT_REACHED_MSG, ex.getMessage());
        verify(reservationService, times(1)).setPaymentFailedForReservationsInBasket(basketId);
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
