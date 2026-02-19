package pl.dgrecki.models.requests;

import static pl.dgrecki.constants.ExceptionMessages.*;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddReservationRequest {

    @NotNull(message = BASKET_ID_IS_REQUIRED_MSG)
    private final UUID basketId;

    @NotNull(message = SCREENING_ID_IS_REQUIRED_MSG)
    private final UUID screeningId;

    @NotNull(message = SEAT_ID_IS_REQUIRED_MSG)
    private final UUID seatId;
}
