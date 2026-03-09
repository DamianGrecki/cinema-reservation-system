package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.exceptions.TicketCreatingException;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.entities.Ticket;
import pl.dgrecki.models.enums.ReservationStatus;
import pl.dgrecki.repositories.TicketRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketPdfJobService ticketPdfJobService;
    private final ReservationService reservationService;
    private final Clock clock;

    @Transactional
    public void createTickets(Order order) {
        List<Reservation> reservations = reservationService.getPaidReservationsByOrder(order);
        if (reservations.isEmpty()) {
            log.warn("Cannot create tickets, there is no paid reservations in order: '{}'", order.getId());
        }
        reservations.forEach(this::createTicket);
    }

    public List<Ticket> getTicketsByOrderId(UUID orderId) {
        return ticketRepository.findByOrderId(orderId);
    }

    public Ticket getTicketById(UUID ticketId) {
        return ticketRepository
                .findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(TICKET_NOT_FOUND_MSG, ticketId)));
    }

    private void createTicket(Reservation reservation) {
        validateTicketUniqueness(reservation);

        Ticket ticket = Ticket.builder()
                .order(reservation.getOrder())
                .reservation(reservation)
                .price(new BigDecimal("20.00")) // TODO Add single ticket price
                .createdAt(clock.instant())
                .build();

        ticketRepository.save(ticket);
        reservation.setStatus(ReservationStatus.PAID);
        ticketPdfJobService.createTicketPdfJob(ticket.getId());
    }

    private void validateTicketUniqueness(Reservation reservation) {
        if (ticketRepository.findByReservation(reservation).isPresent()) {
            throw new TicketCreatingException(TICKET_ALREADY_EXISTS_MSG);
        }
    }
}
