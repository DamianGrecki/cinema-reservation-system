package pl.dgrecki.models.requests;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.dgrecki.models.enums.TicketType;

@Getter
@AllArgsConstructor
public class CreateTicketRequest {
    private final UUID reservationId;
    private final TicketType ticketType;
}
