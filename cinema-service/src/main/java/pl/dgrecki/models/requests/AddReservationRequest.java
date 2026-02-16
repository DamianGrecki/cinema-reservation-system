package pl.dgrecki.models.requests;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static pl.dgrecki.constants.ExceptionMessages.*;

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
