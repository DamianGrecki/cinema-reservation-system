package pl.dgrecki.services.prices;

import org.springframework.stereotype.Component;
import pl.dgrecki.models.entities.PriceModifier;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.PriceModifierType;

@Component
public class TicketPricingModifierStrategy implements PriceModifierStrategy {

    @Override
    public PriceModifierType supports() {
        return PriceModifierType.PRICING_TYPE;
    }

    @Override
    public boolean matches(PriceModifier modifier, Reservation reservation) {
        return modifier.getSubjectType().equals(reservation.getPricingType().name());
    }
}
