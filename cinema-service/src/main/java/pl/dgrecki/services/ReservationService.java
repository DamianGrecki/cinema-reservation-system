package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.PricingType.NORMAL;
import static pl.dgrecki.models.enums.ReservationStatus.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ReservationProcessException;
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.Basket;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.entities.Screening;
import pl.dgrecki.models.entities.Seat;
import pl.dgrecki.models.enums.ReservationStatus;
import pl.dgrecki.models.requests.AddReservationRequest;
import pl.dgrecki.models.requests.PatchReservationPricingTypeRequest;
import pl.dgrecki.models.responses.ReservationResponse;
import pl.dgrecki.models.responses.SuccessResponse;
import pl.dgrecki.repositories.ReservationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BasketService basketService;
    private final ScreeningService screeningService;
    private final SeatService seatService;
    private final Clock clock;

    @Transactional
    public ReservationResponse addReservation(AddReservationRequest request) {
        Basket basket = basketService.getById(request.getBasketId());
        validateBasket(basket);

        Screening screening = screeningService.getById(request.getScreeningId());
        Seat seat = seatService.getById(request.getSeatId());

        validateShowStartTime(screening);
        validateSeatAndScreening(screening, seat);
        validateReservationUniqueness(screening, seat);

        Instant now = clock.instant();
        Reservation reservation = Reservation.builder()
                .basket(basket)
                .screening(screening)
                .seat(seat)
                .status(CREATED)
                .pricingType(NORMAL)
                .createdAt(now)
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(basket.getId(), savedReservation.getId());
    }

    @Transactional
    public SuccessResponse updateReservationPricingType(
            UUID reservationId, PatchReservationPricingTypeRequest request) {
        Reservation reservation = getReservationById(reservationId);
        reservation.setPricingType(request.pricingType());
        return new SuccessResponse();
    }

    @Transactional
    public SuccessResponse cancelReservations(List<UUID> reservationsIds) {
        List<Reservation> reservations = reservationRepository.findAllById(reservationsIds);

        if (reservations.isEmpty()) {
            throw new ReservationProcessException(String.format(RESERVATIONS_NOT_FOUND_MSG));
        }

        List<UUID> invalidReservationsIds = reservations.stream()
                .filter(r -> !r.getStatus().canTransitionTo(CANCELED))
                .map(Reservation::getId)
                .toList();

        if (!invalidReservationsIds.isEmpty()) {
            throw new ReservationProcessException(
                    String.format(RESERVATIONS_CANCEL_FAILED_MSG, invalidReservationsIds));
        }

        reservations.forEach(r -> r.setStatus(CANCELED));
        return new SuccessResponse();
    }

    @Transactional
    public void setExpireStatusOnOverdueReservations(int limit) {
        List<UUID> basketsIds = basketService.getExpiredBasketsIds(limit);
        if (!basketsIds.isEmpty()) {
            List<Reservation> reservations =
                    reservationRepository.claimReservationsByBasketIds(basketsIds, CREATED.name());
            if (!reservations.isEmpty()) {
                List<UUID> ids = reservations.stream().map(Reservation::getId).toList();
                int updatedCount = reservationRepository.setReservationsStatus(ids, EXPIRED);
                log.info("Expired {} reservations", updatedCount);
            }
        }
    }

    @Transactional
    public void deleteExpiredReservationsByBasketsIds(List<UUID> basketsIds) {
        List<UUID> reservationsIds = reservationRepository.findReservationsIdsByBasketIdsAndStatus(basketsIds, EXPIRED);
        if (!reservationsIds.isEmpty()) {
            int deletedCount = reservationRepository.deleteReservationsByIdAndStatus(reservationsIds, EXPIRED);
            log.info("Deleted {} expired reservations", deletedCount);
        }
    }

    @Transactional
    public List<Reservation> claimReservationsByStatusAndBasketId(UUID basketId, ReservationStatus status) {
        return reservationRepository.claimReservationsByBasketIds(List.of(basketId), status.name());
    }

    public Reservation getReservationById(UUID id) {
        Optional<Reservation> reservationOptional = reservationRepository.findById(id);
        if (reservationOptional.isEmpty()) {
            throw new ResourceNotFoundException(String.format(RESERVATION_NOT_FOUND_MSG, id));
        }
        return reservationOptional.get();
    }

    private void validateBasket(Basket basket) {
        if (clock.instant().isAfter(basket.getExpiresAt())) {
            throw new ReservationProcessException(BASKET_EXPIRED_MSG);
        }
    }

    private void validateReservationUniqueness(Screening screening, Seat seat) {
        if (reservationRepository.existsByScreeningIdAndSeatIdAndStatusIn(
                screening.getId(), seat.getId(), List.of(CREATED, PENDING_PAYMENT, PAID))) {
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
