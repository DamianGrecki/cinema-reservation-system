package pl.dgrecki.models.requests;

import jakarta.validation.constraints.NotBlank;
import pl.dgrecki.models.enums.PricingType;

public record UpdateReservationPricingTypeRequest(@NotBlank PricingType pricingType) {}
