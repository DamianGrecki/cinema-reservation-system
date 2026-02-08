package pl.dgrecki.models.requests;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddReservationRequest {
    private final UUID screeningId;
    private final UUID seatId;
}
