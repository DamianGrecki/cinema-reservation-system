package pl.dgrecki.models.external;

import java.util.UUID;
import pl.dgrecki.models.enums.OrderStatus;

public record SandboxPaymentResponse(UUID transactionId, UUID orderId, OrderStatus status) {}
