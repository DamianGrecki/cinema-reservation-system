package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.TicketDownloadException;
import pl.dgrecki.models.TicketFileDto;
import pl.dgrecki.models.entities.Ticket;
import pl.dgrecki.services.storage.TicketFileStorage;

@Service
@RequiredArgsConstructor
public class TicketDownloadService {

    private final TicketService ticketService;
    private final TicketDownloadTokenService tokenService;
    private final TicketFileStorage ticketFileStorage;

    public TicketFileDto getTicketPdf(UUID ticketId, String signature) {
        isValidSignature(ticketId, signature);

        Ticket ticket = ticketService.getTicketById(ticketId);
        if (ticket.getFileName() == null) {
            throw new TicketDownloadException(TICKET_PDF_NOT_GENERATED_MSG);
        }

        byte[] content = ticketFileStorage.load(ticket.getFileName());
        return new TicketFileDto(content, ticket.getFileName());
    }

    private void isValidSignature(UUID ticketId, String signature) {
        if (!tokenService.isValidSignature(ticketId, signature)) {
            throw new TicketDownloadException(INVALID_TICKET_DOWNLOAD_LINK_MSG);
        }
    }
}
