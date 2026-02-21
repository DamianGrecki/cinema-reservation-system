package pl.dgrecki.models.responses;

import java.util.List;
import java.util.UUID;

public record RowSeatsMapResponse(
        int rowNumber,
        List<UUID> seatsIds) {}
