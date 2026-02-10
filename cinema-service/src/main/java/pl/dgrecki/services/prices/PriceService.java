package pl.dgrecki.services.prices;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import pl.dgrecki.models.entities.PriceModifier;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.PriceModifierType;
import pl.dgrecki.models.enums.TicketType;
import pl.dgrecki.repositories.PriceModifierRepository;

@Service
public class PriceService {

    private final PriceModifierRepository priceModifierRepository;
    private final Map<PriceModifierType, PriceModifierStrategy> strategies;

    public PriceService(PriceModifierRepository priceModifierRepository, List<PriceModifierStrategy> strategyList) {
        this.priceModifierRepository = priceModifierRepository;
        this.strategies =
                strategyList.stream().collect(Collectors.toMap(PriceModifierStrategy::supports, Function.identity()));
    }

    public BigDecimal calculatePrice(Reservation reservation, TicketType ticketType) {
        return priceModifierRepository.findAll().stream()
                .filter(m -> strategies.get(m.getModifierType()).matches(m, reservation, ticketType))
                .map(PriceModifier::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
