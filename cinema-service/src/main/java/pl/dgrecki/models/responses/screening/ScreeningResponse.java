package pl.dgrecki.models.responses.screening;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.dgrecki.models.enums.AudioLanguage;
import pl.dgrecki.models.enums.PresentationType;

@Getter
@AllArgsConstructor
public class ScreeningResponse {
    private UUID id;
    private String cinemaHallName;
    private String movieTitle;
    private String movieFormat;
    private PresentationType presentationType;
    private AudioLanguage audioLanguage;
    private Instant startTime;
    private Instant endTime;
}
