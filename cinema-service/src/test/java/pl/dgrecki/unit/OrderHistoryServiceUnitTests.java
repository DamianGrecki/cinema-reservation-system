package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dgrecki.models.entities.*;
import pl.dgrecki.models.enums.Currency;
import pl.dgrecki.models.enums.PaymentProvider;
import pl.dgrecki.models.enums.RefundStatus;
import pl.dgrecki.models.responses.OrderHistoryResponse;
import pl.dgrecki.models.responses.OrderHistoryTicketResponse;
import pl.dgrecki.repositories.CustomerRepository;
import pl.dgrecki.repositories.RefundRepository;
import pl.dgrecki.repositories.TicketRepository;
import pl.dgrecki.services.OrderHistoryService;

@ExtendWith(MockitoExtension.class)
class OrderHistoryServiceUnitTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private RefundRepository refundRepository;

    @InjectMocks
    private OrderHistoryService orderHistoryService;

    @Test
    void shouldReturnEmptyWhenCustomerNotFoundTest() {
        Long userId = 1L;
        when(customerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        List<OrderHistoryResponse> result = orderHistoryService.getHistoryForUser(userId);

        assertTrue(result.isEmpty());
        verifyNoInteractions(ticketRepository);
        verifyNoInteractions(refundRepository);
    }

    @Test
    void shouldReturnEmptyWhenNoTicketsTest() {
        Long userId = 1L;
        Customer customer = customer(userId);
        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));
        when(ticketRepository.findHistoryByCustomerId(customer.getId())).thenReturn(List.of());

        List<OrderHistoryResponse> result = orderHistoryService.getHistoryForUser(userId);

        assertTrue(result.isEmpty());
        verifyNoInteractions(refundRepository);
    }

    @Test
    void shouldGroupTicketsByOrderAndMapAllFieldsTest() {
        Long userId = 1L;
        Customer customer = customer(userId);

        Order order1 = order(BigDecimal.valueOf(60), Instant.parse("2026-05-01T10:00:00Z"));
        Order order2 = order(BigDecimal.valueOf(30), Instant.parse("2026-05-02T10:00:00Z"));

        Ticket t1 = ticket(order1, "Inception", "Sala A", "Multikino", 5, 7, BigDecimal.valueOf(30));
        Ticket t2 = ticket(order1, "Inception", "Sala A", "Multikino", 5, 8, BigDecimal.valueOf(30));
        Ticket t3 = ticket(order2, "Tenet", "Sala B", "Cinema City", 1, 1, BigDecimal.valueOf(30));

        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));
        when(ticketRepository.findHistoryByCustomerId(customer.getId())).thenReturn(List.of(t1, t2, t3));
        when(refundRepository.findOrderIdsWithRefundStatus(any(), eq(RefundStatus.COMPLETED)))
                .thenReturn(Set.of(order2.getId()));

        List<OrderHistoryResponse> result = orderHistoryService.getHistoryForUser(userId);

        assertEquals(2, result.size());

        OrderHistoryResponse first = result.get(0);
        assertEquals(order1.getId(), first.getOrderId());
        assertEquals(BigDecimal.valueOf(60), first.getTotalPrice());
        assertEquals(Currency.PLN, first.getCurrency());
        assertFalse(first.isRefunded());
        assertEquals(2, first.getTickets().size());

        OrderHistoryTicketResponse firstTicket = first.getTickets().get(0);
        assertEquals(t1.getId(), firstTicket.getTicketId());
        assertEquals("Inception", firstTicket.getMovieTitle());
        assertEquals("Multikino", firstTicket.getCinemaName());
        assertEquals("Sala A", firstTicket.getHallName());
        assertEquals(5, firstTicket.getSeatRow());
        assertEquals(7, firstTicket.getSeatNumber());
        assertEquals(BigDecimal.valueOf(30), firstTicket.getPrice());

        OrderHistoryResponse second = result.get(1);
        assertEquals(order2.getId(), second.getOrderId());
        assertTrue(second.isRefunded());
        assertEquals(1, second.getTickets().size());
        assertEquals("Tenet", second.getTickets().get(0).getMovieTitle());
    }

    @Test
    void shouldMarkAllAsNotRefundedWhenNoRefundsTest() {
        Long userId = 1L;
        Customer customer = customer(userId);
        Order order = order(BigDecimal.valueOf(30), Instant.now());
        Ticket ticket = ticket(order, "Movie", "Hall", "Cinema", 1, 1, BigDecimal.valueOf(30));

        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));
        when(ticketRepository.findHistoryByCustomerId(customer.getId())).thenReturn(List.of(ticket));
        when(refundRepository.findOrderIdsWithRefundStatus(any(), eq(RefundStatus.COMPLETED)))
                .thenReturn(Set.of());

        List<OrderHistoryResponse> result = orderHistoryService.getHistoryForUser(userId);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRefunded());
    }

    private Customer customer(Long userId) {
        Customer customer = Customer.builder()
                .userId(userId)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .createdAt(Instant.now())
                .build();
        ReflectionTestUtils.setField(customer, "id", UUID.randomUUID());
        return customer;
    }

    private Order order(BigDecimal price, Instant createdAt) {
        Order order = Order.builder()
                .price(price)
                .currency(Currency.PLN)
                .provider(PaymentProvider.SANDBOX)
                .createdAt(createdAt)
                .build();
        ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
        return order;
    }

    private Ticket ticket(
            Order order,
            String movieTitle,
            String hallName,
            String cinemaName,
            int rowNumber,
            int seatNumber,
            BigDecimal price) {
        Cinema cinema = Cinema.builder()
                .name(cinemaName)
                .city("City")
                .createdAt(Instant.now())
                .build();
        CinemaHall hall = CinemaHall.builder()
                .name(hallName)
                .cinema(cinema)
                .createdAt(Instant.now())
                .build();
        Movie movie = Movie.builder().title(movieTitle).createdAt(Instant.now()).build();
        MovieVersion movieVersion = MovieVersion.builder().movie(movie).build();
        Screening screening = Screening.builder()
                .cinemaHall(hall)
                .movieVersion(movieVersion)
                .startTime(Instant.parse("2026-06-01T19:00:00Z"))
                .endTime(Instant.parse("2026-06-01T21:00:00Z"))
                .build();
        HallRow row = HallRow.builder()
                .cinemaHall(hall)
                .rowNumber(rowNumber)
                .seatsCount(20)
                .build();
        Seat seat = Seat.builder().hallRow(row).seatNumber(seatNumber).build();
        Reservation reservation = Reservation.builder()
                .screening(screening)
                .seat(seat)
                .order(order)
                .build();
        Ticket ticket = Ticket.builder()
                .order(order)
                .reservation(reservation)
                .price(price)
                .createdAt(Instant.now())
                .build();
        ReflectionTestUtils.setField(ticket, "id", UUID.randomUUID());
        return ticket;
    }
}
