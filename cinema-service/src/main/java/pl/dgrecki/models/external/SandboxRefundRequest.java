package pl.dgrecki.models.external;

import java.util.UUID;

public record SandboxRefundRequest(String transactionId, UUID orderId) {}
