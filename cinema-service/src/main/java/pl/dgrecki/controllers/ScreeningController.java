package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.SCREENINGS_ENDPOINT;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.responses.screening.ScreeningListResponse;
import pl.dgrecki.services.ScreeningService;

@RestController
@RequestMapping(SCREENINGS_ENDPOINT)
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;

    @GetMapping
    public ResponseEntity<ScreeningListResponse> getScreenings() {
        return ResponseEntity.ok(screeningService.getScreeningsList());
    }
}
