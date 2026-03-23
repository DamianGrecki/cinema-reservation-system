package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.TICKET_NOT_FOUND_MSG;
import static pl.dgrecki.utils.PdfGenerator.generatePdf;
import static pl.dgrecki.utils.QrCodeGenerator.generateBase64QrCode;

import java.time.Clock;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.TicketPdfDto;
import pl.dgrecki.models.entities.*;
import pl.dgrecki.repositories.TicketRepository;
import pl.dgrecki.services.storage.TicketDownloadUrlService;
import pl.dgrecki.services.storage.TicketFileStorage;

@Slf4j
@Service
public class TicketGeneratorService {

    private final TicketRepository ticketRepository;
    private final SpringTemplateEngine templateEngine;
    private final TicketPdfMapper ticketPdfMapper;
    private final TicketTemplateService ticketTemplateService;
    private final TicketFileStorage ticketFileStorage;
    private final TicketPdfJobService ticketPdfJobService;
    private final TicketDownloadUrlService ticketDownloadUrlService;
    private final EventService eventService;
    private final CustomerService customerService;
    private final Clock clock;

    public TicketGeneratorService(
            TicketRepository ticketRepository,
            TicketPdfMapper ticketPdfMapper,
            TicketTemplateService ticketTemplateService,
            TicketFileStorage ticketFileStorage,
            TicketPdfJobService ticketPdfJobService,
            TicketDownloadUrlService ticketDownloadUrlService,
            EventService eventService,
            CustomerService customerService,
            Clock clock) {
        this.ticketRepository = ticketRepository;
        this.ticketPdfMapper = ticketPdfMapper;
        this.templateEngine = getTemplateEngine();
        this.ticketTemplateService = ticketTemplateService;
        this.ticketFileStorage = ticketFileStorage;
        this.ticketPdfJobService = ticketPdfJobService;
        this.ticketDownloadUrlService = ticketDownloadUrlService;
        this.eventService = eventService;
        this.customerService = customerService;
        this.clock = clock;
    }

    @Transactional
    public void createTicketPdf(TicketPdfJob ticketPdfJob) {
        UUID ticketId = ticketPdfJob.getTicketId();
        Ticket ticket = ticketRepository
                .findDataForTicketPdf(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(TICKET_NOT_FOUND_MSG, ticketId)));
        String html = buildTicketHtml(ticket);
        byte[] pdfBytes = generatePdf(html);
        String fileName = ticketFileStorage.store(ticketId, pdfBytes);
        ticket.setFileName(fileName);
        ticketPdfJobService.markGenerated(ticketPdfJob);
        publishEventIfAllTicketsGenerated(ticket);
    }

    private void publishEventIfAllTicketsGenerated(Ticket ticket) {
        UUID orderId = ticket.getOrder().getId();
        if (ticketPdfJobService.areAllTicketsGeneratedForOrder(orderId)) {
            Order order = ticket.getOrder();
            String recipientEmail = resolveRecipientEmail(order);
            String recipientFirstName = resolveRecipientFirstName(order);
            String downloadUrl = ticketDownloadUrlService.generateUrl(orderId);
            eventService.createTicketGeneratedEvent(orderId, recipientEmail, recipientFirstName, downloadUrl);
            log.info("All tickets generated for order {}, outbox event created", orderId);
        }
    }

    private String resolveRecipientEmail(Order order) {
        if (order.getCustomerId() != null) {
            return customerService.getById(order.getCustomerId()).getEmail();
        }
        if (order.getGuestEmail() != null) {
            return order.getGuestEmail();
        }
        throw new IllegalStateException("No recipient email for order: " + order.getId());
    }

    private String resolveRecipientFirstName(Order order) {
        if (order.getCustomerId() != null) {
            return customerService.getById(order.getCustomerId()).getFirstName();
        }
        if (order.getGuestFirstName() != null) {
            return order.getGuestFirstName();
        }
        throw new IllegalStateException("No recipient first name for order: " + order.getId());
    }

    private String buildTicketHtml(Ticket ticket) {
        TicketTemplate ticketTemplate = ticketTemplateService.getActiveTemplate();

        String qrCodeInBase64 = generateBase64QrCode(
                ticket.getId().toString(), ticketTemplate.getQrWidth(), ticketTemplate.getQrHeight());

        TicketPdfDto ticketPdf = ticketPdfMapper.map(ticket, qrCodeInBase64, clock.getZone());

        Context context = new Context();
        context.setVariable("ticket", ticketPdf);

        return templateEngine.process(ticketTemplate.getTemplateHtml(), context);
    }

    private SpringTemplateEngine getTemplateEngine() {
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("HTML");
        resolver.setCacheable(false);

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }
}
