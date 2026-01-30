package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.ReservationStatus.CREATED;
import static pl.dgrecki.models.enums.ReservationStatus.PAID;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.ReservationProcessException;
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.entities.Screening;
import pl.dgrecki.models.entities.Seat;
import pl.dgrecki.models.requests.ReservationRequest;
import pl.dgrecki.models.responses.SuccessResponse;
import pl.dgrecki.repositories.ReservationRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final Duration RESERVATION_DURATION = Duration.ofMinutes(15);

    private final ReservationRepository reservationRepository;
    private final ScreeningService screeningService;
    private final SeatService seatService;
    private final Clock clock;

    public SuccessResponse addReservation(ReservationRequest request) {
        Screening screening = screeningService.getById(request.getScreeningId());
        Seat seat = seatService.getById(request.getSeatId());

        validateSeatAndScreening(screening, seat);
        validateReservationUniqueness(screening, seat);

        Instant now = Instant.now(clock);
        Reservation reservation = Reservation.builder()
                .screening(screening)
                .seat(seat)
                .priceInCents(0)
                .status(CREATED)
                .expiresAt(now.plus(RESERVATION_DURATION))
                .createdAt(now)
                .build();
        reservationRepository.save(reservation);
        return new SuccessResponse();
    }

    private void validateReservationUniqueness(Screening screening, Seat seat) {
        if (reservationRepository.existsByScreeningIdAndSeatIdAndStatusIn(
                screening.getId(), seat.getId(), List.of(CREATED, PAID))) {
            throw new ResourceAlreadyExistsException(RESERVATION_ALREADY_EXISTS_MSG);
        }
    }

    private void validateSeatAndScreening(Screening screening, Seat seat) {
        if (!screening.getCinemaHall().equals(seat.getHallRow().getCinemaHall())) {
            throw new ReservationProcessException(SEAT_DOES_NOT_BELONG_TO_HALL_MSG);
        }
    }
}
