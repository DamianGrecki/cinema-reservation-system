package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;

import java.math.BigDecimal;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.TicketCreatingException;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.entities.Ticket;
import pl.dgrecki.models.enums.ReservationStatus;
import pl.dgrecki.models.requests.CreateTicketRequest;
import pl.dgrecki.models.responses.SuccessResponse;
import pl.dgrecki.repositories.TicketRepository;
import pl.dgrecki.services.prices.PriceService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketGeneratorService ticketGeneratorService;
    private final ReservationService reservationService;
    private final PriceService priceService;
    private final Clock clock;

    @Transactional
    public SuccessResponse createTicket(CreateTicketRequest request) {
        Reservation reservation = reservationService.getReservationById(request.getReservationId());
        BigDecimal price = priceService.calculatePrice(reservation, request.getTicketType());

        validateTicketUniqueness(reservation);
        validateReservationStatus(reservation);

        Ticket ticket = Ticket.builder()
                .price(price)
                .reservation(reservation)
                .createdAt(clock.instant())
                .build();

        ticketRepository.save(ticket);
        reservation.setStatus(ReservationStatus.PAID);
        ticketGeneratorService.createTicketPdf(ticket.getId());
        return new SuccessResponse();
    }

    private void validateReservationStatus(Reservation reservation) {
        if (reservation.getStatus().equals(ReservationStatus.EXPIRED)) {
            throw new TicketCreatingException(RESERVATION_EXPIRED_MSG);
        }
        if (reservation.getStatus().equals(ReservationStatus.PAID)) {
            throw new TicketCreatingException(RESERVATION_PAID_MSG);
        }
    }

    private void validateTicketUniqueness(Reservation reservation) {
        if (ticketRepository.findByReservation(reservation).isPresent()) {
            throw new TicketCreatingException(TICKET_ALREADY_EXISTS_MSG);
        }
    }
}
