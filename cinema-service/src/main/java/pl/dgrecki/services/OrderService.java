package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.Currency.PLN;
import static pl.dgrecki.models.enums.ReservationStatus.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    public Order prepareOrder(
            UUID customerId, String guestEmail, String guestFirstName, UUID basketId, PaymentProvider provider) {
        List<Reservation> reservations = claimReservationsForOrder(basketId);
        reservations.forEach(r -> r.setStatus(PAYMENT_PENDING));

        validateBasket(basketId);

        return resolveOrder(reservations, customerId, guestEmail, guestFirstName, provider);
    }

    private Order resolveOrder(
            List<Reservation> reservations,
            UUID customerId,
            String guestEmail,
            String guestFirstName,
            PaymentProvider provider) {
        return reservations.stream()
                .map(Reservation::getOrder)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> {
                    BigDecimal amount = priceService.calculateReservationsTotalPrice(reservations);
                    Order newOrder = createOrder(customerId, guestEmail, guestFirstName, amount, PLN, provider);
                    reservations.forEach(r -> r.setOrder(newOrder));
                    return newOrder;
                });
    }

    private Order createOrder(
            UUID customerId,
            String guestEmail,
            String guestFirstName,
            BigDecimal price,
            Currency currency,
            PaymentProvider provider) {
        Order order = Order.builder()
                .customerId(customerId)
                .guestEmail(guestEmail)
                .guestFirstName(guestFirstName)
                .price(price)
                .currency(currency)
                .provider(provider)
                .createdAt(clock.instant())
                .build();
        return orderRepository.save(order);
    }

    private List<Reservation> claimReservationsForOrder(UUID basketId) {
        List<Reservation> reservations = reservationService.claimReservationsByStatusesAndBasketId(
                basketId, ReservationStatus.statusesThatCanTransitionTo(PAYMENT_PENDING));
        if (reservations.isEmpty()) {
            throw new PaymentProcessException(RESERVATIONS_NOT_FOUND_MSG);
        }
        return reservations;
    }

    @Transactional(readOnly = true)
    public Order getById(UUID orderId) {
        return orderRepository
                .findById(orderId)
                .orElseThrow(() -> new PaymentProcessException(String.format(ORDER_NOT_FOUND_MSG, orderId)));
    }

    @Transactional(readOnly = true)
    public Optional<Order> findExistingOrderByBasketId(UUID basketId) {
        return orderRepository.findByBasketId(basketId);
    }

    private void validateBasket(UUID basketId) {
        Basket basket = basketService.getById(basketId);
        if (basket.getExpiresAt().isBefore(clock.instant())) {
            throw new PaymentProcessException(BASKET_EXPIRED_MSG);
        }
    }
}
