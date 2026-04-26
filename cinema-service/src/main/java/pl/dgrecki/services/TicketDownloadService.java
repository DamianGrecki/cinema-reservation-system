package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.TicketDownloadException;
import pl.dgrecki.models.entities.Ticket;
import pl.dgrecki.services.storage.TicketDownloadUrlService;
import pl.dgrecki.services.storage.TicketFileStorage;

@Service
@RequiredArgsConstructor
public class TicketDownloadService {

    private final TicketService ticketService;
    private final TicketDownloadUrlService ticketUrlService;
    private final TicketFileStorage ticketFileStorage;

    @Transactional(readOnly = true)
    public byte[] getTicketsZip(UUID orderId, String signature) {
        validateSignature(orderId, signature);

        List<Ticket> tickets = ticketService.getTicketsByOrderId(orderId);
        if (tickets.isEmpty()) {
            throw new TicketDownloadException(TICKETS_BY_ORDER_NOT_FOUND_MSG.formatted(orderId));
        }

        return buildZip(tickets);
    }

    private byte[] buildZip(List<Ticket> tickets) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

            for (Ticket ticket : tickets) {
                if (ticket.getFileName() == null) {
                    throw new TicketDownloadException(TICKET_PDF_NOT_GENERATED_MSG);
                }
                byte[] pdfContent = ticketFileStorage.load(ticket.getFileName());
                zipOutputStream.putNextEntry(new ZipEntry(ticket.getFileName()));
                zipOutputStream.write(pdfContent);
                zipOutputStream.closeEntry();
            }

            zipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to build tickets ZIP", e);
        }
    }

    private void validateSignature(UUID orderId, String signature) {
        if (!ticketUrlService.isValidSignature(orderId, signature)) {
            throw new TicketDownloadException(INVALID_TICKET_DOWNLOAD_LINK_MSG);
        }
    }
}
