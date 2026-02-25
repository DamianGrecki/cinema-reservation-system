package pl.dgrecki.services.payments;

import static pl.dgrecki.constants.ExceptionMessages.PAYMENT_ATTEMPTS_LIMIT_REACHED_MSG;
import static pl.dgrecki.constants.ExceptionMessages.PAYMENT_ATTEMPT_ALREADY_EXISTS_MSG;
import static pl.dgrecki.models.enums.PaymentStatus.FAILED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.services.ReservationService;

@Service
@RequiredArgsConstructor
public class PaymentFailureService {

    private final PaymentAttemptService paymentAttemptService;
    private final ReservationService reservationService;

    @Transactional
    public void failPaymentAttempt(Order order, String transactionId, String errorMessage) {
        paymentAttemptService.createAttempt(order, transactionId, FAILED, errorMessage);
        if (paymentAttemptService.isAttemptsLimitReached(order)) {
            reservationService.setPaymentFailedForReservationsByOrder(order);
            return;
        }
        reservationService.setPaymentAttemptFailedForReservationsByOrder(order);
    }

    public void failPaymentAfterRetryLimit(Order order) {
        if (paymentAttemptService.isAttemptsLimitReached(order)) {
            reservationService.setPaymentFailedForReservationsByOrder(order);
            throw new PaymentProcessException(PAYMENT_ATTEMPTS_LIMIT_REACHED_MSG);
        }
    }

    public void stopNewPaymentIfPendingAttemptExists(Order order) {
        if (paymentAttemptService.hasPendingPaymentAttempt(order)) {
            throw new PaymentProcessException(PAYMENT_ATTEMPT_ALREADY_EXISTS_MSG);
        }
    }
}
