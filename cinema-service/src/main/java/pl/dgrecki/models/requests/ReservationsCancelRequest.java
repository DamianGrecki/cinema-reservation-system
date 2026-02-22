package pl.dgrecki.models.requests;

import static pl.dgrecki.constants.ExceptionMessages.RESERVATIONS_IDS_CANNOT_BE_EMPTY_MSG;
import static pl.dgrecki.constants.ExceptionMessages.RESERVATIONS_IDS_IS_REQUIRED_MSG;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ReservationsCancelRequest(
        @NotNull(message = RESERVATIONS_IDS_IS_REQUIRED_MSG) @NotEmpty(message = RESERVATIONS_IDS_CANNOT_BE_EMPTY_MSG)
        List<UUID> reservationsIds) {}
