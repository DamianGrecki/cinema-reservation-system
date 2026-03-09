package pl.dgrecki.models.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ReservationStatus {
    CREATED,
    PAYMENT_PENDING,
    PAYMENT_ATTEMPT_FAILED,
    PAYMENT_FAILED,
    PAID,
    EXPIRED,
    CANCELED,
    REFUNDED;

    public boolean canTransitionTo(ReservationStatus target) {
        return allowedTransitions().contains(target);
    }

    public static Set<ReservationStatus> statusesThatCanTransitionTo(ReservationStatus finalStatus) {
        return Arrays.stream(values())
                .filter(s -> s.allowedTransitions().contains(finalStatus))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Set<ReservationStatus> getStatusesBlockingSeat() {
        return Set.of(CREATED, PAYMENT_PENDING, PAYMENT_ATTEMPT_FAILED, PAID);
    }

    private Set<ReservationStatus> allowedTransitions() {
        return switch (this) {
            case CREATED -> Set.of(PAYMENT_PENDING, CANCELED, EXPIRED);
            case PAYMENT_PENDING -> Set.of(PAYMENT_ATTEMPT_FAILED, PAID, CANCELED, PAYMENT_FAILED);
            case PAYMENT_ATTEMPT_FAILED -> Set.of(PAYMENT_PENDING, PAYMENT_FAILED, EXPIRED);
            case PAID -> Set.of(REFUNDED);
            case EXPIRED, CANCELED, PAYMENT_FAILED, REFUNDED -> Set.of();
        };
    }
}
