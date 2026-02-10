package pl.dgrecki.services;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dgrecki.models.entities.PriceModifier;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.TicketType;
import pl.dgrecki.repositories.PriceModifierRepository;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final PriceModifierRepository priceModifierRepository;

    public BigDecimal calculatePrice(Reservation reservation, TicketType ticketType) {
        BigDecimal basePrice = BigDecimal.ZERO;
        Set<PriceModifier> priceModifiers = getModifiers(reservation, ticketType);
        return priceModifiers.stream().map(PriceModifier::getAmount).reduce(basePrice, BigDecimal::add);
    }

    private Set<PriceModifier> getModifiers(Reservation reservation, TicketType ticketType) {
        return priceModifierRepository.findAll().stream()
                .filter(m -> switch (m.getModifierType()) {
                    case SEAT ->
                        m.getSubjectType()
                                .equals(reservation.getSeat().getSeatType().name());
                    case MOVIE_FORMAT ->
                        m.getSubjectType()
                                .equals(reservation
                                        .getScreening()
                                        .getMovieVersion()
                                        .getMovieFormat()
                                        .name());
                    case TICKET_TYPE -> m.getSubjectType().equals(ticketType.name());
                })
                .collect(Collectors.toSet());
    }
}
