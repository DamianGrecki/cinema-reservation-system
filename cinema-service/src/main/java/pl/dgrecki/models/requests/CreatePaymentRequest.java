package pl.dgrecki.models.requests;

import static pl.dgrecki.constants.ExceptionMessages.*;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import pl.dgrecki.models.enums.PaymentProvider;

public record CreatePaymentRequest(
        @NotNull(message = BASKET_ID_IS_REQUIRED_MSG) UUID basketId,

        @NotNull(message = PROVIDER_IS_REQUIRED_MSG) PaymentProvider provider,

        UUID customerId,

        String guestEmail,

        String guestFirstName) {}
