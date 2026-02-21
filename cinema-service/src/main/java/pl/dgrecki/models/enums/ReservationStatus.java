package pl.dgrecki.models.enums;

import java.util.Set;

public enum ReservationStatus {
    CREATED,
    PENDING_PAYMENT,
    PAID,
    EXPIRED,
    CANCELED;

    public boolean canTransitionTo(ReservationStatus target) {
        return allowedTransitions().contains(target);
    }

    private Set<ReservationStatus> allowedTransitions() {
        return switch (this) {
            case CREATED -> Set.of(PENDING_PAYMENT, CANCELED, EXPIRED);
            case PENDING_PAYMENT -> Set.of(PAID, CANCELED);
            case PAID, EXPIRED, CANCELED -> Set.of();
        };
    }
}
