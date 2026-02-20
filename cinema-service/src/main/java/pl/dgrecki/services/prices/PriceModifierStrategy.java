package pl.dgrecki.services.prices;

import pl.dgrecki.models.entities.PriceModifier;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.PriceModifierType;

public interface PriceModifierStrategy {

    PriceModifierType supports();

    boolean matches(PriceModifier modifier, Reservation reservation);
}
