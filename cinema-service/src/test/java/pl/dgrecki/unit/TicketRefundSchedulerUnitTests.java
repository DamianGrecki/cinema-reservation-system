package pl.dgrecki.unit;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.models.enums.RefundReason;
import pl.dgrecki.schedulers.TicketRefundScheduler;
import pl.dgrecki.services.payments.RefundService;

@ExtendWith(MockitoExtension.class)
class TicketRefundSchedulerUnitTests {

    @Mock
    private RefundService refundService;

    @InjectMocks
    private TicketRefundScheduler ticketRefundScheduler;

    @Test
    void processRefundsTest() {
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        when(refundService.findOrderIdsReadyToRefund()).thenReturn(List.of(orderId1, orderId2));

        ticketRefundScheduler.processRefunds();

        verify(refundService).refundOrder(orderId1, RefundReason.TICKET_GENERATION_FAILED);
        verify(refundService).refundOrder(orderId2, RefundReason.TICKET_GENERATION_FAILED);
    }

    @Test
    void processRefundsWhenNoOrdersShouldNotCallRefundTest() {
        when(refundService.findOrderIdsReadyToRefund()).thenReturn(List.of());

        ticketRefundScheduler.processRefunds();

        verify(refundService, never()).refundOrder(any(), any());
    }

    @Test
    void processRefundsWhenOneFailsShouldContinueWithOthersTest() {
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        when(refundService.findOrderIdsReadyToRefund()).thenReturn(List.of(orderId1, orderId2));
        doThrow(new RuntimeException("Refund failed"))
                .when(refundService)
                .refundOrder(orderId1, RefundReason.TICKET_GENERATION_FAILED);

        ticketRefundScheduler.processRefunds();

        verify(refundService).refundOrder(orderId1, RefundReason.TICKET_GENERATION_FAILED);
        verify(refundService).refundOrder(orderId2, RefundReason.TICKET_GENERATION_FAILED);
    }
}
