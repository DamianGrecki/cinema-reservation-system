package pl.dgrecki.models.requests;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateTicketRequest {
    private final UUID reservationId;
}
