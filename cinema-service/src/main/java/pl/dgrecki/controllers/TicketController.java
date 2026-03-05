package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.TICKET_DOWNLOAD_ENDPOINT;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.TicketFileDto;
import pl.dgrecki.services.TicketDownloadService;

@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketDownloadService ticketDownloadService;

    @GetMapping(TICKET_DOWNLOAD_ENDPOINT)
    public ResponseEntity<byte[]> downloadTicket(@PathVariable UUID ticketId, @RequestParam String signature) {

        TicketFileDto ticketFile = ticketDownloadService.getTicketPdf(ticketId, signature);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ticketFile.fileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(ticketFile.content().length)
                .body(ticketFile.content());
    }
}
