package pl.dgrecki.models.requests;

import static pl.dgrecki.constants.ExceptionMessages.*;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddReservationRequest(
        @NotNull(message = BASKET_ID_IS_REQUIRED_MSG) UUID basketId,
        @NotNull(message = SCREENING_ID_IS_REQUIRED_MSG) UUID screeningId,
        @NotNull(message = SEAT_ID_IS_REQUIRED_MSG) UUID seatId) {}
