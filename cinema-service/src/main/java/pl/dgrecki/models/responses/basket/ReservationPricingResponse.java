package pl.dgrecki.models.responses.basket;

import java.math.BigDecimal;
import java.util.UUID;
import pl.dgrecki.models.enums.PricingType;

public record ReservationPricingResponse(UUID reservationId, PricingType pricingType, BigDecimal price) {}
