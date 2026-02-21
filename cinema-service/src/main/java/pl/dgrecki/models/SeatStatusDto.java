package pl.dgrecki.models;

import java.util.UUID;

public record SeatStatusDto(int seatNumber, UUID seatId, boolean reserved) {}
