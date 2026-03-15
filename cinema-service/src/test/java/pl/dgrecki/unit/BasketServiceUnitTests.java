package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.ReservationPrice;
import pl.dgrecki.models.entities.Basket;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.PricingType;
import pl.dgrecki.models.responses.BasketResponse;
import pl.dgrecki.models.responses.basket.BasketPricingResponse;
import pl.dgrecki.repositories.BasketRepository;
import pl.dgrecki.repositories.ReservationRepository;
import pl.dgrecki.services.BasketService;
import pl.dgrecki.services.prices.PriceService;

@ExtendWith(MockitoExtension.class)
class BasketServiceUnitTests {

    @Mock
    private BasketRepository basketRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PriceService priceService;

    @Mock
    private Clock clock;

    @InjectMocks
    private BasketService basketService;

    private static final Instant FIXED_NOW = Instant.parse("2025-01-01T10:00:00Z");

    @Test
    void addBasketTest() {
        UUID basketId = UUID.randomUUID();

        when(clock.instant()).thenReturn(FIXED_NOW);

        Basket basket = Basket.builder()
                .id(basketId)
                .expiresAt(FIXED_NOW.plus(Duration.ofMinutes(15)))
                .createdAt(FIXED_NOW)
                .build();

        when(basketRepository.save(any(Basket.class))).thenReturn(basket);

        BasketResponse response = basketService.addBasket();

        ArgumentCaptor<Basket> captor = ArgumentCaptor.forClass(Basket.class);
        verify(basketRepository, times(1)).save(captor.capture());

        Basket savedBasket = captor.getValue();

        assertEquals(FIXED_NOW, savedBasket.getCreatedAt());
        assertEquals(FIXED_NOW.plus(Duration.ofMinutes(15)), savedBasket.getExpiresAt());

        assertEquals(basketId, response.getBasketId());
    }

    @Test
    void deleteExpiredBasketsWhenNoExpiredIdsShouldNotCallDeleteTest() {
        Duration duration = Duration.ofMinutes(30);
        Instant expectedExpireOlderThan = FIXED_NOW.minus(duration);

        when(clock.instant()).thenReturn(FIXED_NOW);
        when(reservationRepository.findExpiredBasketsIdsWithoutReservations(
                        eq(expectedExpireOlderThan), any(Pageable.class)))
                .thenReturn(List.of());

        basketService.deleteExpiredBaskets(duration, 5);

        verify(reservationRepository, times(1))
                .findExpiredBasketsIdsWithoutReservations(
                        eq(expectedExpireOlderThan), argThat(pageable -> pageable.getPageSize() == 5));

        verify(basketRepository, never()).deleteExpiredBasketsWithReservations(anyList(), any());
    }

    @Test
    void deleteExpiredBasketsWhenExpiredIdsExistShouldDeleteTest() {
        Duration duration = Duration.ofMinutes(30);
        UUID id = UUID.randomUUID();
        Instant expectedExpireOlderThan = FIXED_NOW.minus(duration);

        when(clock.instant()).thenReturn(FIXED_NOW);

        when(reservationRepository.findExpiredBasketsIdsWithoutReservations(
                        eq(expectedExpireOlderThan), any(Pageable.class)))
                .thenReturn(List.of(id));

        when(basketRepository.deleteExpiredBasketsWithReservations(eq(List.of(id)), eq(expectedExpireOlderThan)))
                .thenReturn(1);

        basketService.deleteExpiredBaskets(duration, 10);

        verify(basketRepository).deleteExpiredBasketsWithReservations(eq(List.of(id)), eq(expectedExpireOlderThan));
    }

    @Test
    void getByIdWhenExistsShouldReturnBasketTest() {
        UUID id = UUID.randomUUID();
        Basket basket = Basket.builder().id(id).build();

        when(basketRepository.findById(id)).thenReturn(Optional.of(basket));

        Basket result = basketService.getById(id);

        assertEquals(id, result.getId());

        verify(basketRepository, times(1)).findById(id);
    }

    @Test
    void getByIdWhenNotExistsShouldThrowExceptionTest() {
        UUID id = UUID.randomUUID();

        when(basketRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> basketService.getById(id));

        verify(basketRepository, times(1)).findById(id);
    }

    @Test
    void getExpiredBasketsIdsTest() {
        UUID id = UUID.randomUUID();

        when(clock.instant()).thenReturn(FIXED_NOW);
        when(basketRepository.findIdsByExpiresAtBefore(eq(FIXED_NOW), any(Pageable.class)))
                .thenReturn(List.of(id));

        List<UUID> result = basketService.getExpiredBasketsIds(5);

        assertEquals(1, result.size());
        assertEquals(id, result.getFirst());

        verify(basketRepository, times(1)).findIdsByExpiresAtBefore(eq(FIXED_NOW), any(Pageable.class));
    }

    @Test
    void getBasketPricingShouldReturnPricingPerReservationAndTotalTest() {
        UUID basketId = UUID.randomUUID();
        UUID reservationId1 = UUID.randomUUID();
        UUID reservationId2 = UUID.randomUUID();

        Reservation r1 = Reservation.builder()
                .id(reservationId1)
                .pricingType(PricingType.NORMAL)
                .build();
        Reservation r2 = Reservation.builder()
                .id(reservationId2)
                .pricingType(PricingType.REDUCED)
                .build();
        List<Reservation> reservations = List.of(r1, r2);

        when(reservationRepository.findByBasketIdAndStatusIn(eq(basketId), any(Collection.class)))
                .thenReturn(reservations);
        when(priceService.calculatePricesPerReservation(reservations))
                .thenReturn(List.of(
                        new ReservationPrice(r1, new BigDecimal("25.00")),
                        new ReservationPrice(r2, new BigDecimal("15.00"))));

        BasketPricingResponse result = basketService.getBasketPricing(basketId);

        assertEquals(2, result.reservations().size());
        assertEquals(reservationId1, result.reservations().get(0).reservationId());
        assertEquals(PricingType.NORMAL, result.reservations().get(0).pricingType());
        assertEquals(new BigDecimal("25.00"), result.reservations().get(0).price());
        assertEquals(reservationId2, result.reservations().get(1).reservationId());
        assertEquals(PricingType.REDUCED, result.reservations().get(1).pricingType());
        assertEquals(new BigDecimal("15.00"), result.reservations().get(1).price());
        assertEquals(new BigDecimal("40.00"), result.totalPrice());
    }

    @Test
    void getBasketPricingWhenNoReservationsShouldReturnEmptyWithZeroTotalTest() {
        UUID basketId = UUID.randomUUID();

        when(reservationRepository.findByBasketIdAndStatusIn(eq(basketId), any(Collection.class)))
                .thenReturn(List.of());
        when(priceService.calculatePricesPerReservation(List.of())).thenReturn(List.of());

        BasketPricingResponse result = basketService.getBasketPricing(basketId);

        assertTrue(result.reservations().isEmpty());
        assertEquals(BigDecimal.ZERO, result.totalPrice());
    }

    @Test
    void getExpiredBasketsIdsOlderThanTest() {
        UUID id = UUID.randomUUID();
        Duration duration = Duration.ofMinutes(10);

        Instant expectedInstant = FIXED_NOW.minus(duration);

        when(clock.instant()).thenReturn(FIXED_NOW);
        when(basketRepository.findIdsByExpiresAtBefore(eq(expectedInstant), any(Pageable.class)))
                .thenReturn(List.of(id));

        List<UUID> result = basketService.getExpiredBasketsIdsOlderThan(duration, 5);

        assertEquals(1, result.size());
        assertEquals(id, result.getFirst());

        verify(basketRepository, times(1)).findIdsByExpiresAtBefore(eq(expectedInstant), any(Pageable.class));
    }
}
