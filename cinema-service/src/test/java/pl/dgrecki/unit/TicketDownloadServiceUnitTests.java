package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.TicketDownloadException;
import pl.dgrecki.models.TicketFileDto;
import pl.dgrecki.models.entities.Ticket;
import pl.dgrecki.services.TicketDownloadService;
import pl.dgrecki.services.TicketDownloadTokenService;
import pl.dgrecki.services.TicketService;
import pl.dgrecki.services.storage.TicketFileStorage;

@ExtendWith(MockitoExtension.class)
class TicketDownloadServiceUnitTests {

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketDownloadTokenService tokenService;

    @Mock
    private TicketFileStorage ticketFileStorage;

    @InjectMocks
    private TicketDownloadService ticketDownloadService;

    @Test
    void getTicketPdfShouldReturnFileWhenSignatureValidAndFileExistsTest() {
        UUID ticketId = UUID.randomUUID();
        String signature = "validSignature";
        String fileName = ticketId + "_ticket.pdf";
        byte[] pdfContent = "pdf-content".getBytes();

        Ticket ticket = Ticket.builder().id(ticketId).fileName(fileName).build();

        when(tokenService.isValidSignature(ticketId, signature)).thenReturn(true);
        when(ticketService.getTicketById(ticketId)).thenReturn(ticket);
        when(ticketFileStorage.load(fileName)).thenReturn(pdfContent);

        TicketFileDto result = ticketDownloadService.getTicketPdf(ticketId, signature);

        assertArrayEquals(pdfContent, result.content());
        assertEquals(fileName, result.fileName());
    }

    @Test
    void getTicketPdfShouldThrowExceptionWhenSignatureInvalidTest() {
        UUID ticketId = UUID.randomUUID();
        String signature = "invalidSignature";

        when(tokenService.isValidSignature(ticketId, signature)).thenReturn(false);

        assertThrows(TicketDownloadException.class, () -> ticketDownloadService.getTicketPdf(ticketId, signature));

        verify(ticketService, never()).getTicketById(any());
    }

    @Test
    void getTicketPdfShouldThrowExceptionWhenFileNameIsNullTest() {
        UUID ticketId = UUID.randomUUID();
        String signature = "validSignature";

        Ticket ticket = Ticket.builder().id(ticketId).fileName(null).build();

        when(tokenService.isValidSignature(ticketId, signature)).thenReturn(true);
        when(ticketService.getTicketById(ticketId)).thenReturn(ticket);

        assertThrows(TicketDownloadException.class, () -> ticketDownloadService.getTicketPdf(ticketId, signature));

        verify(ticketFileStorage, never()).load(any());
    }
}
