package pl.dgrecki.models.responses.basket;

import java.math.BigDecimal;
import java.util.List;

public record BasketPricingResponse(List<ReservationPricingResponse> reservations, BigDecimal totalPrice) {}
