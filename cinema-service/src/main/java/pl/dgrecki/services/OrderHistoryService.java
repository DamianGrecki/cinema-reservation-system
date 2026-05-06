package pl.dgrecki.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.Customer;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.Reservation;
import pl.dgrecki.models.entities.Screening;
import pl.dgrecki.models.entities.Ticket;
import pl.dgrecki.models.enums.RefundStatus;
import pl.dgrecki.models.responses.OrderHistoryResponse;
import pl.dgrecki.models.responses.OrderHistoryTicketResponse;
import pl.dgrecki.repositories.CustomerRepository;
import pl.dgrecki.repositories.RefundRepository;
import pl.dgrecki.repositories.TicketRepository;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final CustomerRepository customerRepository;
    private final TicketRepository ticketRepository;
    private final RefundRepository refundRepository;

    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> getHistoryForUser(Long userId) {
        Optional<Customer> customer = customerRepository.findByUserId(userId);
        if (customer.isEmpty()) {
            return List.of();
        }
        List<Ticket> tickets =
                ticketRepository.findHistoryByCustomerId(customer.get().getId());
        if (tickets.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<Ticket>> ticketsByOrder = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getOrder().getId(), LinkedHashMap::new, Collectors.toList()));
        Set<UUID> refundedOrderIds =
                refundRepository.findOrderIdsWithRefundStatus(ticketsByOrder.keySet(), RefundStatus.COMPLETED);

        return ticketsByOrder.entrySet().stream()
                .map(e -> toOrderResponse(e.getValue(), refundedOrderIds.contains(e.getKey())))
                .toList();
    }

    private OrderHistoryResponse toOrderResponse(List<Ticket> tickets, boolean refunded) {
        Order order = tickets.get(0).getOrder();
        List<OrderHistoryTicketResponse> ticketResponses =
                tickets.stream().map(this::toTicketResponse).toList();
        return new OrderHistoryResponse(
                order.getId(), order.getCreatedAt(), order.getPrice(), order.getCurrency(), refunded, ticketResponses);
    }

    private OrderHistoryTicketResponse toTicketResponse(Ticket ticket) {
        Reservation reservation = ticket.getReservation();
        Screening screening = reservation.getScreening();
        return new OrderHistoryTicketResponse(
                ticket.getId(),
                screening.getMovieVersion().getMovie().getTitle(),
                screening.getStartTime(),
                screening.getCinemaHall().getCinema().getName(),
                screening.getCinemaHall().getName(),
                reservation.getSeat().getHallRow().getRowNumber(),
                reservation.getSeat().getSeatNumber(),
                ticket.getPrice());
    }
}
