package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.RESERVATION_ENDPOINT;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.dgrecki.models.requests.AddReservationRequest;
import pl.dgrecki.models.responses.ReservationResponse;
import pl.dgrecki.services.ReservationService;

@RestController
@RequestMapping(RESERVATION_ENDPOINT)
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody AddReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.addReservation(request));
    }
}
