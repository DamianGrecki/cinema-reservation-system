package pl.dgrecki.models.external;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import pl.dgrecki.models.enums.PaymentStatus;

public record SandboxPaymentResponse(
        @NotNull UUID transactionId,
        @NotNull UUID orderId,
        @NotNull PaymentStatus status,
        String providerError) {}
