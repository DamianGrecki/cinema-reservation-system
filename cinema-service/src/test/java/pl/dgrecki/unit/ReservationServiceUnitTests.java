package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.models.enums.ReservationStatus.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.*;
import pl.dgrecki.models.entities.*;
import pl.dgrecki.models.enums.PricingType;
import pl.dgrecki.models.requests.AddReservationRequest;
import pl.dgrecki.models.requests.PatchReservationPricingTypeRequest;
import pl.dgrecki.models.responses.ReservationResponse;
import pl.dgrecki.repositories.ReservationRepository;
import pl.dgrecki.services.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceUnitTests {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BasketService basketService;

    @Mock
    private ScreeningService screeningService;

    @Mock
    private SeatService seatService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReservationService reservationService;

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void addReservationTest() {
        UUID basketId = UUID.randomUUID();
        UUID screeningId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();

        Basket basket = Basket.builder()
                .id(basketId)
                .expiresAt(FIXED_NOW.plus(Duration.ofMinutes(10)))
                .build();

        CinemaHall hall = new CinemaHall();
        Screening screening = Screening.builder()
                .id(screeningId)
                .startTime(FIXED_NOW.plus(Duration.ofMinutes(60)))
                .cinemaHall(hall)
                .build();

        HallRow row = new HallRow();
        row.setCinemaHall(hall);

        Seat seat = Seat.builder().id(seatId).hallRow(row).build();

        AddReservationRequest request = new AddReservationRequest(basketId, screeningId, seatId);

        when(clock.instant()).thenReturn(FIXED_NOW);
        when(basketService.getById(basketId)).thenReturn(basket);
        when(screeningService.getById(screeningId)).thenReturn(screening);
        when(seatService.getById(seatId)).thenReturn(seat);
        when(reservationRepository.existsByScreeningIdAndSeatIdAndStatusIn(any(), any(), anySet()))
                .thenReturn(false);

        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .screening(screening)
                .seat(seat)
                .basket(basket)
                .status(CREATED)
                .createdAt(FIXED_NOW)
                .build();

        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse response = reservationService.addReservation(request);

        assertEquals(basketId, response.getBasketId());

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(captor.capture());

        Reservation saved = captor.getValue();
        assertEquals(CREATED, saved.getStatus());
        assertEquals(FIXED_NOW, saved.getCreatedAt());
        assertEquals(basket, saved.getBasket());
        assertEquals(screening, saved.getScreening());
        assertEquals(seat, saved.getSeat());
    }

    @Test
    void addReservationWhenBasketExpiredShouldThrowTest() {
        UUID basketId = UUID.randomUUID();

        Basket basket = Basket.builder()
                .id(basketId)
                .expiresAt(FIXED_NOW.minusSeconds(1))
                .build();

        when(clock.instant()).thenReturn(FIXED_NOW);
        when(basketService.getById(basketId)).thenReturn(basket);

        AddReservationRequest request = new AddReservationRequest(basketId, UUID.randomUUID(), UUID.randomUUID());

        ReservationProcessException ex =
                assertThrows(ReservationProcessException.class, () -> reservationService.addReservation(request));

        assertEquals(BASKET_EXPIRED_MSG, ex.getMessage());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void addReservationWhenShowAlreadyStartedShouldThrowTest() {
        UUID basketId = UUID.randomUUID();
        UUID screeningId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();

        Basket basket = Basket.builder()
                .id(basketId)
                .expiresAt(FIXED_NOW.plus(Duration.ofMinutes(10)))
                .build();

        Screening screening = Screening.builder()
                .id(screeningId)
                .startTime(FIXED_NOW.minus(Duration.ofMinutes(31)))
                .cinemaHall(new CinemaHall())
                .build();

        when(clock.instant()).thenReturn(FIXED_NOW);
        when(basketService.getById(basketId)).thenReturn(basket);
        when(screeningService.getById(screeningId)).thenReturn(screening);
        when(seatService.getById(seatId))
                .thenReturn(Seat.builder().hallRow(new HallRow()).build());

        AddReservationRequest request = new AddReservationRequest(basketId, screeningId, seatId);

        ReservationProcessException ex =
                assertThrows(ReservationProcessException.class, () -> reservationService.addReservation(request));

        assertEquals(SHOW_HAS_ALREADY_STARTED_MSG, ex.getMessage());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void addReservationWhenSeatNotBelongToHallShouldThrowTest() {
        UUID basketId = UUID.randomUUID();
        UUID screeningId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();

        Basket basket = Basket.builder().expiresAt(FIXED_NOW.plusSeconds(60)).build();

        CinemaHall hall1 = new CinemaHall();
        hall1.setId(1L);
        CinemaHall hall2 = new CinemaHall();
        hall2.setId(2L);

        Screening screening = Screening.builder()
                .id(screeningId)
                .startTime(FIXED_NOW.plus(Duration.ofMinutes(60)))
                .cinemaHall(hall1)
                .build();

        HallRow row = new HallRow();
        row.setCinemaHall(hall2);

        Seat seat = Seat.builder().id(seatId).hallRow(row).build();

        when(clock.instant()).thenReturn(FIXED_NOW);
        when(basketService.getById(basketId)).thenReturn(basket);
        when(screeningService.getById(screeningId)).thenReturn(screening);
        when(seatService.getById(seatId)).thenReturn(seat);

        AddReservationRequest request = new AddReservationRequest(basketId, screeningId, seatId);

        ReservationProcessException ex =
                assertThrows(ReservationProcessException.class, () -> reservationService.addReservation(request));

        assertEquals(SEAT_DOES_NOT_BELONG_TO_HALL_MSG, ex.getMessage());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void addReservationWhenReservationAlreadyExistsShouldThrowTest() {
        UUID basketId = UUID.randomUUID();
        UUID screeningId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();

        Basket basket = Basket.builder().expiresAt(FIXED_NOW.plusSeconds(60)).build();

        CinemaHall hall = new CinemaHall();

        Screening screening = Screening.builder()
                .id(screeningId)
                .startTime(FIXED_NOW.plus(Duration.ofMinutes(60)))
                .cinemaHall(hall)
                .build();

        HallRow row = new HallRow();
        row.setCinemaHall(hall);

        Seat seat = Seat.builder().id(seatId).hallRow(row).build();

        when(clock.instant()).thenReturn(FIXED_NOW);
        when(basketService.getById(basketId)).thenReturn(basket);
        when(screeningService.getById(screeningId)).thenReturn(screening);
        when(seatService.getById(seatId)).thenReturn(seat);

        when(reservationRepository.existsByScreeningIdAndSeatIdAndStatusIn(any(), any(), anySet()))
                .thenReturn(true);

        AddReservationRequest request = new AddReservationRequest(basketId, screeningId, seatId);

        ResourceAlreadyExistsException ex =
                assertThrows(ResourceAlreadyExistsException.class, () -> reservationService.addReservation(request));

        assertEquals(RESERVATION_ALREADY_EXISTS_MSG, ex.getMessage());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void updateReservationPricingTypeTest() {
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setPricingType(PricingType.NORMAL);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        reservationService.updateReservationPricingType(
                reservationId, new PatchReservationPricingTypeRequest(PricingType.REDUCED));

        assertEquals(PricingType.REDUCED, reservation.getPricingType());
    }

    @Test
    void cancelReservationsTest() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UUID> ids = List.of(id1, id2);

        Reservation r1 = new Reservation();
        r1.setId(id1);
        r1.setStatus(CREATED);

        Reservation r2 = new Reservation();
        r2.setId(id2);
        r2.setStatus(PAYMENT_PENDING);

        when(reservationRepository.findAllById(ids)).thenReturn(List.of(r1, r2));

        reservationService.cancelReservations(ids);

        assertEquals(CANCELED, r1.getStatus());
        assertEquals(CANCELED, r2.getStatus());
    }

    @Test
    void cancelReservationsWhenEmptyShouldThrowTest() {
        List<UUID> ids = List.of(UUID.randomUUID());

        when(reservationRepository.findAllById(ids)).thenReturn(List.of());

        ReservationProcessException ex =
                assertThrows(ReservationProcessException.class, () -> reservationService.cancelReservations(ids));

        assertEquals(RESERVATIONS_NOT_FOUND_MSG, ex.getMessage());
    }

    @Test
    void cancelReservationsWhenCannotTransitionShouldThrowTest() {
        UUID id = UUID.randomUUID();

        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setStatus(PAID);

        when(reservationRepository.findAllById(List.of(id))).thenReturn(List.of(reservation));

        ReservationProcessException ex = assertThrows(
                ReservationProcessException.class, () -> reservationService.cancelReservations(List.of(id)));

        assertEquals(String.format(RESERVATIONS_STATUS_TRANSITION_FAILED_MSG, List.of(id), CANCELED), ex.getMessage());
        assertEquals(PAID, reservation.getStatus());
    }

    @Test
    void setPaymentAttemptFailedForReservationsByOrderShouldUpdateStatusTest() {
        Order order = Order.builder().id(UUID.randomUUID()).build();

        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setStatus(PAYMENT_PENDING);

        when(reservationRepository.findByOrderAndStatusIn(eq(order), anySet())).thenReturn(List.of(reservation));

        reservationService.setPaymentAttemptFailedForReservationsByOrder(order);

        assertEquals(PAYMENT_ATTEMPT_FAILED, reservation.getStatus());
    }

    @Test
    void setPaymentAttemptFailedForReservationsByOrderWhenNoReservationsDoesNothingTest() {
        Order order = Order.builder().id(UUID.randomUUID()).build();

        when(reservationRepository.findByOrderAndStatusIn(eq(order), anySet())).thenReturn(List.of());

        reservationService.setPaymentAttemptFailedForReservationsByOrder(order);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void getReservationByIdWhenExistsShouldReturnTest() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation();
        reservation.setId(id);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.getReservationById(id);

        assertEquals(id, result.getId());

        verify(reservationRepository, times(1)).findById(id);
    }

    @Test
    void getReservationByIdWhenNotExistsShouldThrowTest() {
        UUID id = UUID.randomUUID();

        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> reservationService.getReservationById(id));

        assertEquals(String.format(RESERVATION_NOT_FOUND_MSG, id), ex.getMessage());
        verify(reservationRepository, times(1)).findById(id);
    }
}
