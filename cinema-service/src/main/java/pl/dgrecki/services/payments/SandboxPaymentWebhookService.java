package pl.dgrecki.services.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.external.SandboxPaymentResponse;
import pl.dgrecki.services.ReservationService;
import pl.dgrecki.services.TicketService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SandboxPaymentWebhookService implements PaymentWebhook {

    private final PaymentAttemptService paymentAttemptService;
    private final ReservationService reservationService;
    private final TicketService ticketService;

    @Override
    @Transactional
    public void handleWebhook(SandboxPaymentResponse response) {
        PaymentAttempt attempt = paymentAttemptService.updateAttemptStatus(
                response.transactionId().toString(), response.status());
        Order order = attempt.getOrder();
        log.info(
                "Processing payment webhook for transaction {} with status {}",
                response.transactionId(),
                response.status());
        switch (response.status()) {
            case COMPLETED -> {
                reservationService.setPaidForReservationsByOrder(order);
                ticketService.createTickets(order);
                log.info("Payment completed for order {}", order.getId());
            }
            case FAILED, CANCELED -> {
                reservationService.setPaymentAttemptFailedForReservationsByOrder(order);
                if (paymentAttemptService.isAttemptsLimitReached(order)) {
                    reservationService.setPaymentFailedForReservationsByOrder(order);
                    log.warn("Payment attempts limit reached for order {}", order.getId());
                }
                log.info("Payment {} for order {}", response.status(), order.getId());
            }
            case PENDING -> log.warn("Payment {} still in PENDING status", response.transactionId());
        }
    }
}
