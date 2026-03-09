package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.TicketDownloadException;
import pl.dgrecki.models.entities.Ticket;
import pl.dgrecki.services.TicketDownloadService;
import pl.dgrecki.services.TicketService;
import pl.dgrecki.services.storage.TicketDownloadUrlService;
import pl.dgrecki.services.storage.TicketFileStorage;

@ExtendWith(MockitoExtension.class)
class TicketDownloadServiceUnitTests {

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketDownloadUrlService tokenService;

    @Mock
    private TicketFileStorage ticketFileStorage;

    @InjectMocks
    private TicketDownloadService ticketDownloadService;

    @Test
    void getTicketsZipShouldReturnZipWithTicketsTest() throws IOException {
        UUID orderId = UUID.randomUUID();
        String signature = "validSignature";
        UUID ticketId1 = UUID.randomUUID();
        UUID ticketId2 = UUID.randomUUID();
        String fileName1 = ticketId1 + "_ticket.pdf";
        String fileName2 = ticketId2 + "_ticket.pdf";

        Ticket ticket1 = Ticket.builder().id(ticketId1).fileName(fileName1).build();
        Ticket ticket2 = Ticket.builder().id(ticketId2).fileName(fileName2).build();

        when(tokenService.isValidSignature(orderId, signature)).thenReturn(true);
        when(ticketService.getTicketsByOrderId(orderId)).thenReturn(List.of(ticket1, ticket2));
        when(ticketFileStorage.load(fileName1)).thenReturn("pdf1".getBytes());
        when(ticketFileStorage.load(fileName2)).thenReturn("pdf2".getBytes());

        byte[] zip = ticketDownloadService.getTicketsZip(orderId, signature);

        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry1 = zipInputStream.getNextEntry();
            assertNotNull(entry1);
            assertEquals(fileName1, entry1.getName());

            ZipEntry entry2 = zipInputStream.getNextEntry();
            assertNotNull(entry2);
            assertEquals(fileName2, entry2.getName());

            assertNull(zipInputStream.getNextEntry());
        }
    }

    @Test
    void getTicketsZipShouldThrowExceptionWhenSignatureInvalidTest() {
        UUID orderId = UUID.randomUUID();
        String invalidSignature = "invalidSignature";

        when(tokenService.isValidSignature(orderId, invalidSignature)).thenReturn(false);

        assertThrows(
                TicketDownloadException.class, () -> ticketDownloadService.getTicketsZip(orderId, invalidSignature));

        verify(ticketService, never()).getTicketsByOrderId(any());
    }

    @Test
    void getTicketsZipShouldThrowExceptionWhenNoTicketsFoundTest() {
        UUID orderId = UUID.randomUUID();
        String signature = "validSignature";

        when(tokenService.isValidSignature(orderId, signature)).thenReturn(true);
        when(ticketService.getTicketsByOrderId(orderId)).thenReturn(List.of());

        assertThrows(TicketDownloadException.class, () -> ticketDownloadService.getTicketsZip(orderId, signature));
    }

    @Test
    void getTicketsZipShouldThrowExceptionWhenFileNameIsNullTest() {
        UUID orderId = UUID.randomUUID();
        String signature = "validSignature";

        Ticket ticket = Ticket.builder().id(UUID.randomUUID()).fileName(null).build();

        when(tokenService.isValidSignature(orderId, signature)).thenReturn(true);
        when(ticketService.getTicketsByOrderId(orderId)).thenReturn(List.of(ticket));

        assertThrows(TicketDownloadException.class, () -> ticketDownloadService.getTicketsZip(orderId, signature));

        verify(ticketFileStorage, never()).load(any());
    }
}
