package pl.dgrecki.models.external;

import java.math.BigDecimal;
import java.util.UUID;
import pl.dgrecki.models.enums.Currency;

public record SandboxPaymentRequest(UUID orderId, BigDecimal amount, Currency currency) {}
