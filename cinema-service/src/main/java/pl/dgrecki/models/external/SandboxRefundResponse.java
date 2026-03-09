package pl.dgrecki.models.external;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import pl.dgrecki.models.enums.RefundStatus;

public record SandboxRefundResponse(
        @NotNull UUID transactionId,
        @NotNull UUID orderId,
        @NotNull RefundStatus status) {}
