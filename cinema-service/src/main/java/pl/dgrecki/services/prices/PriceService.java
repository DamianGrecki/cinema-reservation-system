package pl.dgrecki.services.prices;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.ReservationPrice;
import pl.dgrecki.models.entities.PriceModifier;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.enums.PriceModifierType;
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

    @Transactional(readOnly = true)
    public BigDecimal calculateReservationsTotalPrice(List<Reservation> reservations) {
        return calculatePricesPerReservation(reservations).stream()
                .map(ReservationPrice::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<ReservationPrice> calculatePricesPerReservation(List<Reservation> reservations) {
        List<PriceModifier> modifiers = priceModifierRepository.findAll();
        return reservations.stream()
                .map(r -> new ReservationPrice(r, calculate(r, modifiers)))
                .toList();
    }

    private BigDecimal calculate(Reservation reservation, List<PriceModifier> modifiers) {
        return modifiers.stream()
                .filter(m -> strategies.get(m.getModifierType()).matches(m, reservation))
                .map(PriceModifier::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
