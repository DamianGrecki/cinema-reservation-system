package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.SCREENINGS_ENDPOINT;

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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(SCREENINGS_ENDPOINT)
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;
    private final SeatService seatService;

    @GetMapping
    public ResponseEntity<ScreeningListResponse> getScreenings() {
        return ResponseEntity.ok(screeningService.getScreeningsList());
    }

    @GetMapping("/{screeningId}/seats")
    public ResponseEntity<List<RowSeatsMapResponse>> getRowSeatsMap(@PathVariable UUID screeningId) {
        return ResponseEntity.ok(seatService.getRowSeatsMapByScreeningId(screeningId));
    }
}
