package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.Currency.PLN;
import static pl.dgrecki.models.enums.ReservationStatus.PENDING_PAYMENT;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.models.entities.Basket;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.Currency;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.enums.ReservationStatus;
import pl.dgrecki.repositories.OrderRepository;
import pl.dgrecki.services.prices.PriceService;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ReservationService reservationService;
    private final BasketService basketService;
    private final PriceService priceService;
    private final Clock clock;

    @Transactional
    public Order prepareOrder(UUID customerId, String guestEmail, UUID basketId, PaymentProvider provider) {
        List<Reservation> reservations = claimReservations(basketId);
        reservations.forEach(r -> r.setStatus(PENDING_PAYMENT));

        validateBasket(basketId);

        BigDecimal amount = priceService.calculateReservationsTotalPrice(reservations);

        Order savedOrder = createOrder(customerId, guestEmail, amount, PLN, provider);
        reservations.forEach(r -> r.setOrder(savedOrder));
        return savedOrder;
    }

    private Order createOrder(
            UUID customerId, String guestEmail, BigDecimal price, Currency currency, PaymentProvider provider) {
        Order order = Order.builder()
                .customerId(customerId)
                .guestEmail(guestEmail)
                .price(price)
                .currency(currency)
                .provider(provider)
                .createdAt(clock.instant())
                .build();
        return orderRepository.save(order);
    }

    private List<Reservation> claimReservations(UUID basketId) {
        List<Reservation> reservations =
                reservationService.claimReservationsByStatusAndBasketId(basketId, ReservationStatus.CREATED);
        if (reservations.isEmpty()) {
            throw new PaymentProcessException(RESERVATIONS_NOT_FOUND_MSG);
        }
        return reservations;
    }

    private void validateBasket(UUID basketId) {
        Basket basket = basketService.getById(basketId);
        if (basket.getExpiresAt().isBefore(clock.instant())) {
            throw new PaymentProcessException(BASKET_EXPIRED_MSG);
        }
    }
}
