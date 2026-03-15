package pl.dgrecki.models;

import java.math.BigDecimal;
import pl.dgrecki.models.entities.Reservation;

public record ReservationPrice(Reservation reservation, BigDecimal price) {}
