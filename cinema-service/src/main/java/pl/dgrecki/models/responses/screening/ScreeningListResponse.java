package pl.dgrecki.models.responses.screening;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScreeningListResponse {
    private List<ScreeningResponse> screenings;
}
