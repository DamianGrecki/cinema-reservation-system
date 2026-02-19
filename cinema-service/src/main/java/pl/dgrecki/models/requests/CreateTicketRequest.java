package pl.dgrecki.models.requests;

import static pl.dgrecki.constants.ExceptionMessages.*;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.dgrecki.models.enums.PricingType;

@Getter
@AllArgsConstructor
public class CreateTicketRequest {

    @NotNull(message = RESERVATION_ID_IS_REQUIRED_MSG)
    private final UUID reservationId;

    @NotNull(message = TICKET_TYPE_IS_REQUIRED_MSG)
    private final PricingType pricingType;
}
