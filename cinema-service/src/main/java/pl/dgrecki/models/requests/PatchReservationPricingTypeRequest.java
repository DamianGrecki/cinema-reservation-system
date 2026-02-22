package pl.dgrecki.models.requests;

import static pl.dgrecki.constants.ExceptionMessages.PRICING_TYPE_IS_REQUIRED_MSG;

import jakarta.validation.constraints.NotNull;
import pl.dgrecki.models.enums.PricingType;

public record PatchReservationPricingTypeRequest(
        @NotNull(message = PRICING_TYPE_IS_REQUIRED_MSG) PricingType pricingType) {}
