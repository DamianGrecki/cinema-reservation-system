package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.*;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.exceptions.TicketRefundingException;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.entities.Refund;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.enums.RefundReason;
import pl.dgrecki.models.enums.RefundStatus;
import pl.dgrecki.repositories.RefundRepository;
import pl.dgrecki.services.OrderService;
import pl.dgrecki.services.ReservationService;
import pl.dgrecki.services.payments.PaymentAttemptService;
import pl.dgrecki.services.payments.RefundProviderService;
import pl.dgrecki.services.payments.RefundProviderService.RefundProviderResponse;
import pl.dgrecki.services.payments.RefundService;
import pl.dgrecki.services.payments.RefundWriter;

@ExtendWith(MockitoExtension.class)
class RefundServiceUnitTests {

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T12:00:00Z");
    private static final int MAX_REFUND_ATTEMPTS = 3;

    @Mock
    private PaymentAttemptService paymentAttemptService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private OrderService orderService;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private RefundWriter refundWriter;

    @Mock
    private RefundProviderService sandboxRefundProvider;

    @Mock
    private Clock clock;

    private RefundService refundService;

    @BeforeEach
    void setUp() {
        lenient().when(sandboxRefundProvider.getProviderName()).thenReturn(PaymentProvider.SANDBOX);
        refundService = new RefundService(
                paymentAttemptService,
                reservationService,
                orderService,
                refundRepository,
                refundWriter,
                List.of(sandboxRefundProvider),
                clock,
                MAX_REFUND_ATTEMPTS);
    }

    @Test
    void refundOrderTest() {
        UUID orderId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        String paymentTransactionId = UUID.randomUUID().toString();
        String refundTransactionId = UUID.randomUUID().toString();

        Order order =
                Order.builder().id(orderId).provider(PaymentProvider.SANDBOX).build();
        PaymentAttempt attempt = PaymentAttempt.builder()
                .id(paymentAttemptId)
                .transactionId(paymentTransactionId)
                .build();

        when(clock.instant()).thenReturn(FIXED_NOW);
        when(orderService.getById(orderId)).thenReturn(order);
        when(paymentAttemptService.findCompletedByOrderId(orderId)).thenReturn(attempt);
        when(refundRepository.existsByPaymentAttemptIdAndStatus(paymentAttemptId, RefundStatus.COMPLETED))
                .thenReturn(false);
        when(sandboxRefundProvider.sendRefund(attempt, order))
                .thenReturn(new RefundProviderResponse(refundTransactionId, RefundStatus.COMPLETED));

        refundService.refundOrder(orderId, RefundReason.TICKET_GENERATION_FAILED);

        ArgumentCaptor<Refund> captor = ArgumentCaptor.forClass(Refund.class);
        verify(refundRepository).save(captor.capture());
        Refund savedRefund = captor.getValue();
        assertEquals(RefundStatus.COMPLETED, savedRefund.getStatus());
        assertEquals(RefundReason.TICKET_GENERATION_FAILED, savedRefund.getReason());
        assertEquals(refundTransactionId, savedRefund.getTransactionId());
        assertEquals(FIXED_NOW, savedRefund.getCreatedAt());

        verify(reservationService).setRefundedForReservationsByOrder(order);
    }

    @Test
    void refundOrderWhenRefundAlreadyExistsShouldThrowTest() {
        UUID orderId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();

        Order order =
                Order.builder().id(orderId).provider(PaymentProvider.SANDBOX).build();
        PaymentAttempt attempt = PaymentAttempt.builder().id(paymentAttemptId).build();

        when(orderService.getById(orderId)).thenReturn(order);
        when(paymentAttemptService.findCompletedByOrderId(orderId)).thenReturn(attempt);
        when(refundRepository.existsByPaymentAttemptIdAndStatus(paymentAttemptId, RefundStatus.COMPLETED))
                .thenReturn(true);

        TicketRefundingException ex = assertThrows(
                TicketRefundingException.class,
                () -> refundService.refundOrder(orderId, RefundReason.TICKET_GENERATION_FAILED));

        assertEquals(REFUND_ALREADY_EXISTS_MSG, ex.getMessage());
        verify(sandboxRefundProvider, never()).sendRefund(any(), any());
        verify(refundRepository, never()).save(any());
        verify(reservationService, never()).setRefundedForReservationsByOrder(any());
    }

    @Test
    void refundOrderWhenProviderReturnsNullShouldSaveFailedAndThrowTest() {
        UUID orderId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();

        Order order =
                Order.builder().id(orderId).provider(PaymentProvider.SANDBOX).build();
        PaymentAttempt attempt = PaymentAttempt.builder().id(paymentAttemptId).build();

        when(orderService.getById(orderId)).thenReturn(order);
        when(paymentAttemptService.findCompletedByOrderId(orderId)).thenReturn(attempt);
        when(refundRepository.existsByPaymentAttemptIdAndStatus(paymentAttemptId, RefundStatus.COMPLETED))
                .thenReturn(false);
        when(sandboxRefundProvider.sendRefund(attempt, order)).thenReturn(null);

        TicketRefundingException ex = assertThrows(
                TicketRefundingException.class,
                () -> refundService.refundOrder(orderId, RefundReason.TICKET_GENERATION_FAILED));

        assertEquals(String.format(REFUND_FAILED_MSG, orderId), ex.getMessage());
        verify(refundWriter).saveFailedRefund(attempt, null, RefundReason.TICKET_GENERATION_FAILED);
        verify(refundRepository, never()).save(any());
        verify(reservationService, never()).setRefundedForReservationsByOrder(any());
    }

    @Test
    void refundOrderWhenProviderReturnsFailedStatusShouldSaveFailedAndThrowTest() {
        UUID orderId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        String refundTransactionId = UUID.randomUUID().toString();

        Order order =
                Order.builder().id(orderId).provider(PaymentProvider.SANDBOX).build();
        PaymentAttempt attempt = PaymentAttempt.builder().id(paymentAttemptId).build();

        when(orderService.getById(orderId)).thenReturn(order);
        when(paymentAttemptService.findCompletedByOrderId(orderId)).thenReturn(attempt);
        when(refundRepository.existsByPaymentAttemptIdAndStatus(paymentAttemptId, RefundStatus.COMPLETED))
                .thenReturn(false);
        when(sandboxRefundProvider.sendRefund(attempt, order))
                .thenReturn(new RefundProviderResponse(refundTransactionId, RefundStatus.FAILED));

        TicketRefundingException ex = assertThrows(
                TicketRefundingException.class,
                () -> refundService.refundOrder(orderId, RefundReason.TICKET_GENERATION_FAILED));

        assertEquals(String.format(REFUND_FAILED_MSG, orderId), ex.getMessage());
        verify(refundWriter).saveFailedRefund(attempt, refundTransactionId, RefundReason.TICKET_GENERATION_FAILED);
        verify(refundRepository, never()).save(any());
        verify(reservationService, never()).setRefundedForReservationsByOrder(any());
    }

    @Test
    void refundOrderWhenProviderUnknownShouldThrowTest() {
        UUID orderId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();

        Order order =
                Order.builder().id(orderId).provider(PaymentProvider.STRIPE).build();
        PaymentAttempt attempt = PaymentAttempt.builder().id(paymentAttemptId).build();

        when(orderService.getById(orderId)).thenReturn(order);
        when(paymentAttemptService.findCompletedByOrderId(orderId)).thenReturn(attempt);
        when(refundRepository.existsByPaymentAttemptIdAndStatus(paymentAttemptId, RefundStatus.COMPLETED))
                .thenReturn(false);

        PaymentProcessException ex = assertThrows(
                PaymentProcessException.class,
                () -> refundService.refundOrder(orderId, RefundReason.TICKET_GENERATION_FAILED));

        assertEquals(PROVIDER_IS_UNKNOWN_MSG, ex.getMessage());
        verify(sandboxRefundProvider, never()).sendRefund(any(), any());
        verify(refundRepository, never()).save(any());
        verify(reservationService, never()).setRefundedForReservationsByOrder(any());
    }
}
