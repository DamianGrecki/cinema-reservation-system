package pl.dgrecki.models.requests;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ReservationsCancelRequest(@NotNull List<UUID> reservationsIds) {}
