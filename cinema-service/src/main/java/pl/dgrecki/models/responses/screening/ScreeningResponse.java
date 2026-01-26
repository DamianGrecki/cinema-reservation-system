package pl.dgrecki.models.responses.screening;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScreeningResponse {
    private UUID id;
    private String cinemaHallName;
    private String movieTitle;
    private Instant startTime;
    private Instant endTime;
}
