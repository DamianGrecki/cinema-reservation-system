package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.SCREENINGS_ENDPOINT;
import static pl.dgrecki.constants.Endpoints.SCREENINGS_SEATS_ENDPOINT;
import static pl.dgrecki.constants.Endpoints.SCREENING_ENDPOINT;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.responses.RowSeatsMapResponse;
import pl.dgrecki.models.responses.screening.ScreeningListResponse;
import pl.dgrecki.models.responses.screening.ScreeningResponse;
import pl.dgrecki.services.ScreeningService;
import pl.dgrecki.services.SeatService;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;
    private final SeatService seatService;

    @GetMapping(SCREENINGS_ENDPOINT)
    public ResponseEntity<ScreeningListResponse> getScreenings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(screeningService.getScreeningsListByDate(date));
    }

    @GetMapping(SCREENING_ENDPOINT)
    public ResponseEntity<ScreeningResponse> getScreening(@PathVariable UUID screeningId) {
        return ResponseEntity.ok(screeningService.getScreeningResponseById(screeningId));
    }

    @GetMapping(SCREENINGS_SEATS_ENDPOINT)
    public ResponseEntity<List<RowSeatsMapResponse>> getRowSeatsMap(@PathVariable UUID screeningId) {
        return ResponseEntity.ok(seatService.getRowsSeatsMapByScreeningId(screeningId));
    }
}
