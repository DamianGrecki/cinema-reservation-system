package pl.dgrecki.models.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderHistoryTicketResponse {
    private final UUID ticketId;
    private final String movieTitle;
    private final Instant screeningStart;
    private final String cinemaName;
    private final String hallName;
    private final Integer seatRow;
    private final Integer seatNumber;
    private final BigDecimal price;
}
