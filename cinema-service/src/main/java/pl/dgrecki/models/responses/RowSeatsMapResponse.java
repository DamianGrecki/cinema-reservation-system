package pl.dgrecki.models.responses;

import java.util.List;
import pl.dgrecki.models.SeatStatusDto;

public record RowSeatsMapResponse(int rowNumber, List<SeatStatusDto> seats) {}
