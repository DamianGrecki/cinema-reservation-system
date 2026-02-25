package pl.dgrecki.services.payments;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.external.SandboxPaymentResponse;
import pl.dgrecki.services.ReservationService;
import pl.dgrecki.services.TicketService;

@Service
@RequiredArgsConstructor
public class SandboxPaymentWebhookService implements PaymentWebhook {

    private final PaymentAttemptService paymentAttemptService;
    private final ReservationService reservationService;
    private final TicketService ticketService;

    @Override
    @Transactional
    public void handleWebhook(SandboxPaymentResponse payload) {
        PaymentAttempt attempt = paymentAttemptService.updateAttemptStatus(
                payload.transactionId().toString(), payload.status());
        Order order = attempt.getOrder();
        switch (payload.status()) {
            case COMPLETED -> {
                reservationService.setPaidForReservationsByOrder(order);
                ticketService.createTickets(order);
            }
            case FAILED, CANCELED -> {
                reservationService.setPaymentAttemptFailedForReservationsByOrder(order);
                if (paymentAttemptService.isAttemptsLimitReached(order)) {
                    reservationService.setPaymentFailedForReservationsByOrder(order);
                }
            }
        }
    }
}
