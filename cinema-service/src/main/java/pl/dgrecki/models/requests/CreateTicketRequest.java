package pl.dgrecki.models.requests;

import static pl.dgrecki.constants.ExceptionMessages.*;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import pl.dgrecki.models.enums.PricingType;

public record CreateTicketRequest(
        @NotNull(message = RESERVATION_ID_IS_REQUIRED_MSG) UUID reservationId,
        @NotNull(message = PRICING_TYPE_IS_REQUIRED_MSG) PricingType pricingType) {}
