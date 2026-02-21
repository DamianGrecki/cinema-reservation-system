package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.*;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.dgrecki.models.requests.AddReservationRequest;
import pl.dgrecki.models.requests.ReservationsCancelRequest;
import pl.dgrecki.models.requests.UpdateReservationPricingTypeRequest;
import pl.dgrecki.models.responses.ReservationResponse;
import pl.dgrecki.models.responses.SuccessResponse;
import pl.dgrecki.services.ReservationService;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping(RESERVATION_ENDPOINT)
    public ResponseEntity<ReservationResponse> addReservation(@Valid @RequestBody AddReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.addReservation(request));
    }

    @PostMapping(RESERVATIONS_CANCEL_ENDPOINT)
    public ResponseEntity<SuccessResponse> cancelReservation(@Valid @RequestBody ReservationsCancelRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.cancelReservations(request.reservationsIds()));
    }

    @PatchMapping(RESERVATION_PRICING_TYPE_ENDPOINT)
    public ResponseEntity<SuccessResponse> updateReservationPricingType(
            @PathVariable UUID id, @Valid @RequestBody UpdateReservationPricingTypeRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.updateReservationPricingType(id, request));
    }
}
