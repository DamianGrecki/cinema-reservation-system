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
import pl.dgrecki.exceptions.TicketCreatingException;
import pl.dgrecki.models.ReservationPrice;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.entities.Ticket;
import pl.dgrecki.models.enums.ReservationStatus;
import pl.dgrecki.repositories.TicketRepository;
import pl.dgrecki.services.prices.PriceService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketPdfJobService ticketPdfJobService;
    private final ReservationService reservationService;
    private final PriceService priceService;
    private final Clock clock;

    @Transactional
    public void createTickets(Order order) {
        List<Reservation> reservations = reservationService.getPaidReservationsByOrder(order);
        if (reservations.isEmpty()) {
            log.warn("Cannot create tickets, there is no paid reservations in order: '{}'", order.getId());
            return;
        }
        List<ReservationPrice> prices = priceService.calculatePricesPerReservation(reservations);
        prices.forEach(rp -> createTicket(rp.reservation(), rp.price()));
    }

    public List<Ticket> getTicketsByOrderId(UUID orderId) {
        return ticketRepository.findByOrderId(orderId);
    }

    private void createTicket(Reservation reservation, BigDecimal price) {
        validateTicketUniqueness(reservation);

        Ticket ticket = Ticket.builder()
                .order(reservation.getOrder())
                .reservation(reservation)
                .price(price)
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
