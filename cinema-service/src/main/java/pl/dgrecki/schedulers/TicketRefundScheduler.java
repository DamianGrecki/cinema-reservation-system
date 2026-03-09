package pl.dgrecki.schedulers;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.enums.RefundReason;
import pl.dgrecki.services.payments.RefundService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketRefundScheduler {

    private final RefundService refundService;

    @Scheduled(fixedDelayString = "${scheduler.ticket-refund.delay-ms}")
    public void processRefunds() {
        List<UUID> orderIds = refundService.findOrderIdsReadyToRefund();
        for (UUID orderId : orderIds) {
            try {
                refundService.refundOrder(orderId, RefundReason.TICKET_GENERATION_FAILED);
            } catch (Exception e) {
                log.error("Failed to refund order {}", orderId, e);
            }
        }
    }
}
