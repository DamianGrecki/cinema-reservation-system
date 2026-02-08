package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.ReservationStatus.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ReservationProcessException;
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.entities.Screening;
import pl.dgrecki.models.entities.Seat;
import pl.dgrecki.models.requests.AddReservationRequest;
import pl.dgrecki.models.responses.ReservationResponse;
import pl.dgrecki.repositories.ReservationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final Duration RESERVATION_DURATION = Duration.ofMinutes(15);

    private final ReservationRepository reservationRepository;
    private final ScreeningService screeningService;
    private final SeatService seatService;
    private final Clock clock;

    @Transactional
    public ReservationResponse addReservation(AddReservationRequest request) {
        Screening screening = screeningService.getById(request.getScreeningId());
        Seat seat = seatService.getById(request.getSeatId());

        validateShowStartTime(screening);
        validateSeatAndScreening(screening, seat);
        validateReservationUniqueness(screening, seat);

        Instant now = clock.instant();
        Reservation reservation = Reservation.builder()
                .screening(screening)
                .seat(seat)
                .status(CREATED)
                .expiresAt(now.plus(RESERVATION_DURATION))
                .createdAt(now)
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation.getId());
    }

    @Transactional
    public void setExpireStatusOnOverdueReservations(int limit) {
        List<Reservation> reservations =
                reservationRepository.claimOverdueReservations(CREATED.name(), clock.instant(), limit);
        if (!reservations.isEmpty()) {
            List<UUID> ids = reservations.stream().map(Reservation::getId).toList();
            int updatedCount = reservationRepository.setReservationsStatus(ids, EXPIRED);
            log.info("Expired {} reservations", updatedCount);
        }
    }

    @Transactional
    public void deleteExpiredReservationsOlderThan(Duration expiredDuration, int limit) {
        Instant expireOlderThanDate = clock.instant().minus(expiredDuration);
        Page<UUID> page = reservationRepository.findReservationsIdsByStatusAndOlderThanExpireAt(
                EXPIRED, expireOlderThanDate, Pageable.ofSize(limit));
        if (!page.isEmpty()) {
            List<UUID> ids = page.getContent();
            int deletedCount = reservationRepository.deleteReservationsByIdAndStatus(ids, EXPIRED);
            log.info("Deleted {} expired reservations", deletedCount);
        }
    }

    public Reservation getReservationById(UUID id) {
        Optional<Reservation> reservationOptional = reservationRepository.findById(id);
        if (reservationOptional.isEmpty()) {
            throw new ResourceNotFoundException(String.format(RESERVATION_NOT_FOUND_MSG, id));
        }
        return reservationOptional.get();
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

    private void validateShowStartTime(Screening screening) {
        if (clock.instant().isAfter(screening.getStartTime().plus(Duration.ofMinutes(30)))) {
            throw new ReservationProcessException(SHOW_HAS_ALREADY_STARTED_MSG);
        }
    }
}
