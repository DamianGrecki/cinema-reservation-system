package pl.dgrecki.services.payments;

import static pl.dgrecki.constants.ExceptionMessages.*;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.exceptions.RefundFailedException;
import pl.dgrecki.exceptions.TicketRefundingException;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.entities.Refund;
import pl.dgrecki.models.enums.RefundReason;
import pl.dgrecki.models.enums.RefundStatus;
import pl.dgrecki.models.enums.TicketPdfStatus;
import pl.dgrecki.repositories.RefundRepository;
import pl.dgrecki.services.OrderService;
import pl.dgrecki.services.ReservationService;
import pl.dgrecki.services.payments.RefundProviderService.RefundProviderResponse;

@Slf4j
@Service
public class RefundService {

    private final PaymentAttemptService paymentAttemptService;
    private final ReservationService reservationService;
    private final OrderService orderService;
    private final RefundRepository refundRepository;
    private final List<RefundProviderService> refundProviders;
    private final Clock clock;
    private final int maxRefundAttempts;

    public RefundService(
            PaymentAttemptService paymentAttemptService,
            ReservationService reservationService,
            OrderService orderService,
            RefundRepository refundRepository,
            List<RefundProviderService> refundProviders,
            Clock clock,
            @Value("${scheduler.ticket-refund.max-attempts}") int maxRefundAttempts) {
        this.paymentAttemptService = paymentAttemptService;
        this.reservationService = reservationService;
        this.orderService = orderService;
        this.refundRepository = refundRepository;
        this.refundProviders = refundProviders;
        this.clock = clock;
        this.maxRefundAttempts = maxRefundAttempts;
    }

    public List<UUID> findOrderIdsReadyToRefund() {
        return refundRepository.findOrderIdsReadyToRefund(
                TicketPdfStatus.DEAD, RefundStatus.COMPLETED, RefundStatus.FAILED, maxRefundAttempts);
    }

    @Transactional(noRollbackFor = RefundFailedException.class)
    public void refundOrder(UUID orderId, RefundReason reason) {
        Order order = orderService.getById(orderId);
        PaymentAttempt completedAttempt = paymentAttemptService.findCompletedByOrderId(orderId);
        validateRefundNotExists(completedAttempt);
        log.info("Initiating refund for order {} (transactionId: {})", orderId, completedAttempt.getTransactionId());
        RefundProviderService provider = resolveProvider(order);
        RefundProviderResponse response = provider.sendRefund(completedAttempt, order);
        validateResponseIsNotNull(response, completedAttempt, orderId, reason);
        validateRefundCompleted(response, completedAttempt, orderId, reason);
        saveRefund(completedAttempt, response.transactionId(), RefundStatus.COMPLETED, reason);
        reservationService.setRefundedForReservationsByOrder(order);
        log.info("Refund completed for order {}", orderId);
    }

    private RefundProviderService resolveProvider(Order order) {
        return refundProviders.stream()
                .filter(p -> p.getProviderName().equals(order.getProvider()))
                .findFirst()
                .orElseThrow(() -> new PaymentProcessException(PROVIDER_IS_UNKNOWN_MSG));
    }

    private void validateRefundNotExists(PaymentAttempt completedAttempt) {
        if (refundRepository.existsByPaymentAttemptIdAndStatus(completedAttempt.getId(), RefundStatus.COMPLETED)) {
            throw new TicketRefundingException(REFUND_ALREADY_EXISTS_MSG);
        }
    }

    private void validateResponseIsNotNull(
            RefundProviderResponse response, PaymentAttempt completedAttempt, UUID orderId, RefundReason reason) {
        if (Objects.isNull(response)) {
            saveRefund(completedAttempt, null, RefundStatus.FAILED, reason);
            throw new RefundFailedException(String.format(REFUND_FAILED_MSG, orderId));
        }
    }

    private void validateRefundCompleted(
            RefundProviderResponse response, PaymentAttempt completedAttempt, UUID orderId, RefundReason reason) {
        if (response.status() != RefundStatus.COMPLETED) {
            saveRefund(completedAttempt, response.transactionId(), RefundStatus.FAILED, reason);
            throw new RefundFailedException(String.format(REFUND_FAILED_MSG, orderId));
        }
    }

    private void saveRefund(
            PaymentAttempt paymentAttempt, String transactionId, RefundStatus status, RefundReason reason) {
        Refund refund = Refund.builder()
                .paymentAttempt(paymentAttempt)
                .transactionId(transactionId)
                .status(status)
                .reason(reason)
                .createdAt(clock.instant())
                .build();
        refundRepository.save(refund);
    }
}
