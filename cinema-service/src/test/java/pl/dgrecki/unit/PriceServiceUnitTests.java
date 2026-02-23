package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.models.entities.MovieVersion;
import pl.dgrecki.models.entities.PriceModifier;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.entities.Screening;
import pl.dgrecki.models.entities.Seat;
import pl.dgrecki.models.enums.MovieFormat;
import pl.dgrecki.models.enums.PriceModifierType;
import pl.dgrecki.models.enums.PricingType;
import pl.dgrecki.models.enums.SeatType;
import pl.dgrecki.repositories.PriceModifierRepository;
import pl.dgrecki.services.prices.MovieFormatModifierStrategy;
import pl.dgrecki.services.prices.PriceService;
import pl.dgrecki.services.prices.SeatModifierStrategy;
import pl.dgrecki.services.prices.TicketPricingModifierStrategy;

@ExtendWith(MockitoExtension.class)
class PriceServiceUnitTests {

    @Mock
    private PriceModifierRepository priceModifierRepository;

    private PriceService priceService;

    @BeforeEach
    void setUp() {
        priceService = new PriceService(
                priceModifierRepository,
                List.of(
                        new SeatModifierStrategy(),
                        new MovieFormatModifierStrategy(),
                        new TicketPricingModifierStrategy()));
    }

    @Test
    void calculateReservationsTotalPriceTest() {
        Reservation r1 = buildReservation(SeatType.STANDARD, MovieFormat.FORMAT_2D, PricingType.NORMAL);
        Reservation r2 = buildReservation(SeatType.VIP, MovieFormat.FORMAT_3D, PricingType.REDUCED);

        PriceModifier standardModifier =
                buildModifier(PriceModifierType.SEAT, SeatType.STANDARD.name(), new BigDecimal("10.00"));
        PriceModifier vipModifier = buildModifier(PriceModifierType.SEAT, SeatType.VIP.name(), new BigDecimal("20.00"));

        when(priceModifierRepository.findAll()).thenReturn(List.of(standardModifier, vipModifier));

        BigDecimal result = priceService.calculateReservationsTotalPrice(List.of(r1, r2));

        assertEquals(new BigDecimal("30.00"), result);
    }

    @Test
    void calculateSumOfAllMatchingModifiersTest() {
        Reservation reservation = buildReservation(SeatType.VIP, MovieFormat.FORMAT_3D, PricingType.REDUCED);

        PriceModifier seatModifier =
                buildModifier(PriceModifierType.SEAT, SeatType.VIP.name(), new BigDecimal("20.00"));
        PriceModifier formatModifier =
                buildModifier(PriceModifierType.MOVIE_FORMAT, MovieFormat.FORMAT_3D.name(), new BigDecimal("5.00"));
        PriceModifier pricingModifier =
                buildModifier(PriceModifierType.PRICING_TYPE, PricingType.REDUCED.name(), new BigDecimal("-3.00"));

        when(priceModifierRepository.findAll()).thenReturn(List.of(seatModifier, formatModifier, pricingModifier));

        BigDecimal result = priceService.calculate(reservation);

        assertEquals(new BigDecimal("22.00"), result);
    }

    @Test
    void calculateWhenModifierTypeDoesNotMatchReservationShouldNotIncludeTest() {
        Reservation reservation = buildReservation(SeatType.STANDARD, MovieFormat.FORMAT_2D, PricingType.NORMAL);

        PriceModifier vipModifier = buildModifier(PriceModifierType.SEAT, SeatType.VIP.name(), new BigDecimal("20.00"));

        when(priceModifierRepository.findAll()).thenReturn(List.of(vipModifier));

        BigDecimal result = priceService.calculate(reservation);

        assertEquals(BigDecimal.ZERO, result);
    }

    private Reservation buildReservation(SeatType seatType, MovieFormat movieFormat, PricingType pricingType) {
        Seat seat = Seat.builder().seatType(seatType).build();

        MovieVersion movieVersion =
                MovieVersion.builder().movieFormat(movieFormat).build();

        Screening screening = Screening.builder().movieVersion(movieVersion).build();

        Reservation reservation = new Reservation();
        reservation.setSeat(seat);
        reservation.setScreening(screening);
        reservation.setPricingType(pricingType);
        return reservation;
    }

    private PriceModifier buildModifier(PriceModifierType type, String subjectType, BigDecimal amount) {
        return PriceModifier.builder()
                .modifierType(type)
                .subjectType(subjectType)
                .amount(amount)
                .build();
    }
}
