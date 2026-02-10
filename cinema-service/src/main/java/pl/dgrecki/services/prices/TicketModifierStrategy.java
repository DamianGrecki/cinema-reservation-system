package pl.dgrecki.services.prices;

import org.springframework.stereotype.Component;
import pl.dgrecki.models.entities.PriceModifier;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.PriceModifierType;
import pl.dgrecki.models.enums.TicketType;

@Component
public class TicketModifierStrategy implements PriceModifierStrategy {

    @Override
    public PriceModifierType supports() {
        return PriceModifierType.TICKET_TYPE;
    }

    @Override
    public boolean matches(PriceModifier modifier, Reservation reservation, TicketType ticketType) {
        return modifier.getSubjectType().equals(ticketType.name());
    }
}
