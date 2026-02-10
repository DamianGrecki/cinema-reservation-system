package pl.dgrecki.services.prices;

import org.springframework.stereotype.Component;
import pl.dgrecki.models.entities.PriceModifier;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.PriceModifierType;
import pl.dgrecki.models.enums.TicketType;

@Component
public class MovieFormatModifierStrategy implements PriceModifierStrategy {

    @Override
    public PriceModifierType supports() {
        return PriceModifierType.MOVIE_FORMAT;
    }

    @Override
    public boolean matches(PriceModifier modifier, Reservation reservation, TicketType ticketType) {
        return modifier.getSubjectType()
                .equals(reservation
                        .getScreening()
                        .getMovieVersion()
                        .getMovieFormat()
                        .name());
    }
}
