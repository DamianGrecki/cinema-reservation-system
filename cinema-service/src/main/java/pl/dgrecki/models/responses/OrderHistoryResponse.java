package pl.dgrecki.models.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.dgrecki.models.enums.Currency;

@Getter
@AllArgsConstructor
public class OrderHistoryResponse {
    private final UUID orderId;
    private final Instant createdAt;
    private final BigDecimal totalPrice;
    private final Currency currency;
    private final boolean refunded;
    private final List<OrderHistoryTicketResponse> tickets;
}
