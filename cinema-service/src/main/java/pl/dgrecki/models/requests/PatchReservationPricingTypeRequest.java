package pl.dgrecki.models.requests;

import jakarta.validation.constraints.NotNull;
import pl.dgrecki.models.enums.PricingType;

public record PatchReservationPricingTypeRequest(@NotNull PricingType pricingType) {}
