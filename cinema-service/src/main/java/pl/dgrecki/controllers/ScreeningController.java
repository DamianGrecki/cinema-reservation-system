package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.SCREENINGS_ENDPOINT;
import static pl.dgrecki.constants.Endpoints.SCREENINGS_SEATS_ENDPOINT;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.responses.RowSeatsMapResponse;
import pl.dgrecki.models.responses.screening.ScreeningListResponse;
import pl.dgrecki.services.ScreeningService;
import pl.dgrecki.services.SeatService;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;
    private final SeatService seatService;

    @GetMapping(SCREENINGS_ENDPOINT)
    public ResponseEntity<ScreeningListResponse> getScreenings() {
        return ResponseEntity.ok(screeningService.getScreeningsList());
    }

    @GetMapping(SCREENINGS_SEATS_ENDPOINT)
    public ResponseEntity<List<RowSeatsMapResponse>> getRowSeatsMap(@PathVariable UUID screeningId) {
        return ResponseEntity.ok(seatService.getRowsSeatsMapByScreeningId(screeningId));
    }
}
