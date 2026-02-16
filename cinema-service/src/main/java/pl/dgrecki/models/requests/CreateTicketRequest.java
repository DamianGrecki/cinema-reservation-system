package pl.dgrecki.models.requests;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.dgrecki.models.enums.TicketType;

import static pl.dgrecki.constants.ExceptionMessages.*;

@Getter
@AllArgsConstructor
public class CreateTicketRequest {

    @NotNull(message = RESERVATION_ID_IS_REQUIRED_MSG)
    private final UUID reservationId;

    @NotNull(message = TICKET_TYPE_IS_REQUIRED_MSG)
    private final TicketType ticketType;
}
