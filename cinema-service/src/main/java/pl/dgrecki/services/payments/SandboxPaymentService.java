package pl.dgrecki.services.payments;

import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.Currency.PLN;
import static pl.dgrecki.models.enums.PaymentProvider.SANDBOX;
import static pl.dgrecki.models.enums.ReservationStatus.PENDING_PAYMENT;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import pl.dgrecki.constants.SandboxPaymentProviderEndpoints;
import pl.dgrecki.exceptions.PaymentProcessException;
import pl.dgrecki.models.entities.Basket;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.Currency;
import pl.dgrecki.models.enums.OrderStatus;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.enums.ReservationStatus;
import pl.dgrecki.models.external.SandboxPaymentRequest;
import pl.dgrecki.models.external.SandboxPaymentResponse;
import pl.dgrecki.models.requests.CreatePaymentRequest;
import pl.dgrecki.models.responses.SuccessResponse;
import pl.dgrecki.services.BasketService;
import pl.dgrecki.services.OrderService;
import pl.dgrecki.services.ReservationService;
import pl.dgrecki.services.prices.PriceService;

@Service
@RequiredArgsConstructor
public class SandboxPaymentService implements PaymentProviderService {

    private final OrderService orderService;
    private final PriceService priceService;
    private final ReservationService reservationService;
    private final BasketService basketService;
    private final WebClient sandboxPaymentProviderWebClient;
    private final Clock clock;

    @Override
    public PaymentProvider getProviderName() {
        return SANDBOX;
    }

    @Transactional
    public SuccessResponse createPayment(CreatePaymentRequest request) {
        UUID customerId = request.customerId();
        String guestEmail = request.guestEmail();
        UUID basketId = request.basketId();

        validateCustomerData(customerId, guestEmail);

        List<Reservation> reservations = claimReservations(basketId);
        reservations.forEach(r -> r.setStatus(PENDING_PAYMENT));

        validateBasket(basketId);

        BigDecimal amount = priceService.calculateReservationsTotalPrice(reservations);

        Order savedOrder = orderService.createOrder(customerId, guestEmail, amount, PLN, request.provider());
        reservations.forEach(r -> r.setOrder(savedOrder));

        sendPaymentRequestToProvider(savedOrder.getId(), amount, PLN);
        return new SuccessResponse();
    }

    private void sendPaymentRequestToProvider(UUID orderId, BigDecimal amount, Currency currency) {
        SandboxPaymentRequest body = new SandboxPaymentRequest(orderId, amount, currency);
        SandboxPaymentResponse response = sandboxPaymentProviderWebClient
                .post()
                .uri(SandboxPaymentProviderEndpoints.PAYMENT_ENDPOINT)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(SandboxPaymentResponse.class)
                .block();

        if (Objects.isNull(response)) {
            throw new PaymentProcessException(PROVIDER_NO_RESPONSE_MSG);
        }

        if (!response.status().equals(OrderStatus.PENDING)) {
            throw new PaymentProcessException(PROVIDER_BAD_PAYMENT_STATUS_MSG);
        }
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
            throw new PaymentProcessException(RESERVATION_EXPIRED_MSG);
        }
    }

    private void validateCustomerData(UUID customerId, String guestEmail) {
        if (Objects.isNull(customerId) && Objects.isNull(guestEmail)) {
            throw new PaymentProcessException(CUSTOMER_DATA_REQUIRED_MSG);
        }
    }
}
