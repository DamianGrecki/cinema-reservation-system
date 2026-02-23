package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.ReservationStatus.PENDING_PAYMENT;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.models.entities.Basket;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.Currency;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.enums.ReservationStatus;
import pl.dgrecki.repositories.OrderRepository;
import pl.dgrecki.services.BasketService;
import pl.dgrecki.services.OrderService;
import pl.dgrecki.services.ReservationService;
import pl.dgrecki.services.prices.PriceService;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ReservationService reservationService;

    @Mock
    private BasketService basketService;

    @Mock
    private PriceService priceService;

    @Mock
    private Clock clock;

    @InjectMocks
    private OrderService orderService;

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void prepareOrderTest() {
        UUID customerId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentProvider provider = PaymentProvider.SANDBOX;
        BigDecimal amount = new BigDecimal("25.00");

        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());

        Basket basket = Basket.builder()
                .id(basketId)
                .expiresAt(FIXED_NOW.plus(Duration.ofMinutes(10)))
                .build();

        Order savedOrder = Order.builder()
                .id(orderId)
                .customerId(customerId)
                .price(amount)
                .currency(Currency.PLN)
                .provider(provider)
                .createdAt(FIXED_NOW)
                .build();

        when(clock.instant()).thenReturn(FIXED_NOW);
        when(reservationService.claimReservationsByStatusAndBasketId(basketId, ReservationStatus.CREATED))
                .thenReturn(List.of(reservation));
        when(basketService.getById(basketId)).thenReturn(basket);
        when(priceService.calculateReservationsTotalPrice(anyList())).thenReturn(amount);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.prepareOrder(customerId, null, basketId, provider);

        assertEquals(orderId, result.getId());
        assertEquals(PENDING_PAYMENT, reservation.getStatus());
        assertEquals(savedOrder, reservation.getOrder());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(captor.capture());

        Order captured = captor.getValue();
        assertEquals(customerId, captured.getCustomerId());
        assertEquals(amount, captured.getPrice());
        assertEquals(Currency.PLN, captured.getCurrency());
        assertEquals(provider, captured.getProvider());
        assertEquals(FIXED_NOW, captured.getCreatedAt());
    }

    @Test
    void prepareOrderWhenNoReservationsShouldThrowTest() {
        UUID basketId = UUID.randomUUID();

        when(reservationService.claimReservationsByStatusAndBasketId(basketId, ReservationStatus.CREATED))
                .thenReturn(List.of());

        PaymentProcessException ex = assertThrows(
                PaymentProcessException.class,
                () -> orderService.prepareOrder(UUID.randomUUID(), null, basketId, PaymentProvider.SANDBOX));

        assertEquals(RESERVATIONS_NOT_FOUND_MSG, ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void prepareOrderWhenBasketExpiredShouldThrowTest() {
        UUID basketId = UUID.randomUUID();

        Reservation reservation = new Reservation();

        Basket basket = Basket.builder()
                .id(basketId)
                .expiresAt(FIXED_NOW.minusSeconds(1))
                .build();

        when(clock.instant()).thenReturn(FIXED_NOW);
        when(reservationService.claimReservationsByStatusAndBasketId(basketId, ReservationStatus.CREATED))
                .thenReturn(List.of(reservation));
        when(basketService.getById(basketId)).thenReturn(basket);

        PaymentProcessException ex = assertThrows(
                PaymentProcessException.class,
                () -> orderService.prepareOrder(UUID.randomUUID(), null, basketId, PaymentProvider.SANDBOX));

        assertEquals(BASKET_EXPIRED_MSG, ex.getMessage());
        verify(orderRepository, never()).save(any());
    }
}
