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
import pl.dgrecki.models.TicketPdf;
import pl.dgrecki.models.entities.*;
import pl.dgrecki.repositories.TicketRepository;

@Slf4j
@Service
public class TicketGeneratorService {

    private final TicketRepository ticketRepository;
    private final SpringTemplateEngine templateEngine;
    private final TicketPdfMapper ticketPdfMapper;
    private final TicketTemplateService ticketTemplateService;
    private final Clock clock;

    public TicketGeneratorService(
            TicketRepository ticketRepository,
            TicketPdfMapper ticketPdfMapper,
            TicketTemplateService ticketTemplateService,
            Clock clock) {
        this.ticketRepository = ticketRepository;
        this.ticketPdfMapper = ticketPdfMapper;
        this.templateEngine = getTemplateEngine();
        this.ticketTemplateService = ticketTemplateService;
        this.clock = clock;
    }

    @Transactional
    public void createTicketPdf(UUID ticketId) {
        Ticket ticket = ticketRepository
                .findDataForTicketPdf(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(TICKET_NOT_FOUND_MSG, ticketId)));
        String html = buildTicketHtml(ticket);
        generatePdf(html, String.format("%s_ticket.pdf", ticketId));
    }

    private String buildTicketHtml(Ticket ticket) {
        TicketTemplate ticketTemplate = ticketTemplateService.getActiveTemplate();

        String qrCodeInBase64 = generateBase64QrCode(
                ticket.getId().toString(), ticketTemplate.getQrWidth(), ticketTemplate.getQrHeight());

        TicketPdf ticketPdf = ticketPdfMapper.map(ticket, qrCodeInBase64, clock.getZone());

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
