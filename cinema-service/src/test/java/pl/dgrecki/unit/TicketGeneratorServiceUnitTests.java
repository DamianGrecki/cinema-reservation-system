package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.TicketPdfDto;
import pl.dgrecki.models.entities.*;
import pl.dgrecki.models.entities.Customer;
import pl.dgrecki.repositories.TicketRepository;
import pl.dgrecki.services.*;
import pl.dgrecki.services.storage.TicketDownloadUrlService;
import pl.dgrecki.services.storage.TicketFileStorage;
import pl.dgrecki.utils.PdfGenerator;
import pl.dgrecki.utils.QrCodeGenerator;

@ExtendWith(MockitoExtension.class)
class TicketGeneratorServiceUnitTests {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketPdfMapper ticketPdfMapper;

    @Mock
    private TicketTemplateService ticketTemplateService;

    @Mock
    private TicketFileStorage ticketFileStorage;

    @Mock
    private TicketPdfJobService ticketPdfJobService;

    @Mock
    private TicketDownloadUrlService ticketDownloadUrlService;

    @Mock
    private Clock clock;

    @Mock
    private EventService eventService;

    @Mock
    private CustomerService customerService;

    private TicketGeneratorService ticketGeneratorService;

    @BeforeEach
    void setUp() {
        ticketGeneratorService = new TicketGeneratorService(
                ticketRepository,
                ticketPdfMapper,
                ticketTemplateService,
                ticketFileStorage,
                ticketPdfJobService,
                ticketDownloadUrlService,
                eventService,
                customerService,
                clock);
    }

    @Test
    void createTicketPdfShouldGenerateAndStoreFileTest() {
        UUID ticketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String fileName = ticketId + "_ticket.pdf";
        byte[] pdfBytes = "pdf-content".getBytes();

        TicketPdfJob job = TicketPdfJob.builder().ticketId(ticketId).build();
        Order order =
                Order.builder().id(orderId).guestEmail("guest@example.com").build();
        Ticket ticket = Ticket.builder().id(ticketId).order(order).build();

        stubPdfGeneration(ticketId, ticket);
        when(ticketFileStorage.store(ticketId, pdfBytes)).thenReturn(fileName);
        when(ticketPdfJobService.areAllTicketsGeneratedForOrder(orderId)).thenReturn(false);

        try (MockedStatic<PdfGenerator> pdf = mockStatic(PdfGenerator.class);
                MockedStatic<QrCodeGenerator> qr = mockStatic(QrCodeGenerator.class)) {

            qr.when(() -> QrCodeGenerator.generateBase64QrCode(any(), anyInt(), anyInt()))
                    .thenReturn("qr");
            pdf.when(() -> PdfGenerator.generatePdf(any())).thenReturn(pdfBytes);

            ticketGeneratorService.createTicketPdf(job);
        }

        assertEquals(fileName, ticket.getFileName());
        verify(ticketFileStorage).store(ticketId, pdfBytes);
        verify(ticketPdfJobService).markGenerated(job);
    }

    @Test
    void createTicketPdfShouldPublishEventWhenAllTicketsGeneratedTest() {
        UUID ticketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String email = "guest@example.com";
        String firstName = "Jan";
        String downloadUrl = "http://localhost:8080/api/orders/" + orderId + "/tickets/download?signature=abc";

        TicketPdfJob job = TicketPdfJob.builder().ticketId(ticketId).build();
        Order order = Order.builder()
                .id(orderId)
                .guestEmail(email)
                .guestFirstName(firstName)
                .build();
        Ticket ticket = Ticket.builder().id(ticketId).order(order).build();

        stubPdfGeneration(ticketId, ticket);
        when(ticketFileStorage.store(any(), any())).thenReturn("file.pdf");
        when(ticketPdfJobService.areAllTicketsGeneratedForOrder(orderId)).thenReturn(true);
        when(ticketDownloadUrlService.generateUrl(orderId)).thenReturn(downloadUrl);

        try (MockedStatic<PdfGenerator> pdf = mockStatic(PdfGenerator.class);
                MockedStatic<QrCodeGenerator> qr = mockStatic(QrCodeGenerator.class)) {

            qr.when(() -> QrCodeGenerator.generateBase64QrCode(any(), anyInt(), anyInt()))
                    .thenReturn("qr");
            pdf.when(() -> PdfGenerator.generatePdf(any())).thenReturn("pdf".getBytes());

            ticketGeneratorService.createTicketPdf(job);
        }

        verify(eventService).createTicketGeneratedEvent(orderId, email, firstName, downloadUrl);
    }

    @Test
    void createTicketPdfShouldNotPublishEventWhenNotAllTicketsGeneratedTest() {
        UUID ticketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        TicketPdfJob job = TicketPdfJob.builder().ticketId(ticketId).build();
        Order order =
                Order.builder().id(orderId).guestEmail("guest@example.com").build();
        Ticket ticket = Ticket.builder().id(ticketId).order(order).build();

        stubPdfGeneration(ticketId, ticket);
        when(ticketFileStorage.store(any(), any())).thenReturn("file.pdf");
        when(ticketPdfJobService.areAllTicketsGeneratedForOrder(orderId)).thenReturn(false);

        try (MockedStatic<PdfGenerator> pdf = mockStatic(PdfGenerator.class);
                MockedStatic<QrCodeGenerator> qr = mockStatic(QrCodeGenerator.class)) {

            qr.when(() -> QrCodeGenerator.generateBase64QrCode(any(), anyInt(), anyInt()))
                    .thenReturn("qr");
            pdf.when(() -> PdfGenerator.generatePdf(any())).thenReturn("pdf".getBytes());

            ticketGeneratorService.createTicketPdf(job);
        }

        verify(eventService, never()).createTicketGeneratedEvent(any(), any(), any(), any());
    }

    @Test
    void createTicketPdfShouldPublishEventWithCustomerDataWhenAuthenticatedTest() {
        UUID ticketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String email = "customer@example.com";
        String firstName = "Anna";
        String downloadUrl = "http://localhost:8080/api/orders/" + orderId + "/tickets/download?signature=abc";

        TicketPdfJob job = TicketPdfJob.builder().ticketId(ticketId).build();
        Order order = Order.builder().id(orderId).customerId(customerId).build();
        Ticket ticket = Ticket.builder().id(ticketId).order(order).build();
        Customer customer = Customer.builder()
                .id(customerId)
                .email(email)
                .firstName(firstName)
                .build();

        stubPdfGeneration(ticketId, ticket);
        when(ticketFileStorage.store(any(), any())).thenReturn("file.pdf");
        when(ticketPdfJobService.areAllTicketsGeneratedForOrder(orderId)).thenReturn(true);
        when(ticketDownloadUrlService.generateUrl(orderId)).thenReturn(downloadUrl);
        when(customerService.getById(customerId)).thenReturn(customer);

        try (MockedStatic<PdfGenerator> pdf = mockStatic(PdfGenerator.class);
                MockedStatic<QrCodeGenerator> qr = mockStatic(QrCodeGenerator.class)) {

            qr.when(() -> QrCodeGenerator.generateBase64QrCode(any(), anyInt(), anyInt()))
                    .thenReturn("qr");
            pdf.when(() -> PdfGenerator.generatePdf(any())).thenReturn("pdf".getBytes());

            ticketGeneratorService.createTicketPdf(job);
        }

        verify(customerService, times(2)).getById(customerId);
        verify(eventService).createTicketGeneratedEvent(orderId, email, firstName, downloadUrl);
    }

    @Test
    void createTicketPdfShouldThrowExceptionWhenTicketNotFoundTest() {
        UUID ticketId = UUID.randomUUID();
        TicketPdfJob job = TicketPdfJob.builder().ticketId(ticketId).build();

        when(ticketRepository.findDataForTicketPdf(ticketId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketGeneratorService.createTicketPdf(job));

        verify(ticketFileStorage, never()).store(any(), any());
        verify(ticketPdfJobService, never()).markGenerated(any());
    }

    private void stubPdfGeneration(UUID ticketId, Ticket ticket) {
        TicketTemplate template = TicketTemplate.builder()
                .templateHtml("<html></html>")
                .qrWidth(200)
                .qrHeight(200)
                .build();
        TicketPdfDto ticketPdfDto = new TicketPdfDto(
                "Movie",
                "2D",
                "PL",
                LocalDateTime.now(),
                "Cinema",
                "Warsaw",
                "Hall 1",
                1,
                1,
                BigDecimal.TEN,
                ticketId,
                "qr");

        when(ticketRepository.findDataForTicketPdf(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketTemplateService.getActiveTemplate()).thenReturn(template);
        when(ticketPdfMapper.map(any(), any(), any())).thenReturn(ticketPdfDto);
    }
}
