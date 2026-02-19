package pl.dgrecki.services;

import static pl.dgrecki.models.enums.OrderStatus.PENDING;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.enums.Currency;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.repositories.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final Clock clock;

    @Transactional
    public Order createOrder(
            UUID customerId, String guestEmail, BigDecimal price, Currency currency, PaymentProvider provider) {
        Order order = Order.builder()
                .customerId(customerId)
                .guestEmail(guestEmail)
                .status(PENDING)
                .price(price)
                .currency(currency)
                .provider(provider)
                .createdAt(clock.instant())
                .build();
        return orderRepository.save(order);
    }
}
