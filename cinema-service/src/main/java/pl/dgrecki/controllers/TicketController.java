package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.TICKET_ENDPOINT;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.requests.CreateTicketRequest;
import pl.dgrecki.models.responses.SuccessResponse;
import pl.dgrecki.services.TicketService;

@RestController
@RequestMapping(TICKET_ENDPOINT)
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<SuccessResponse> createTicket(@RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request));
    }
}
