package pl.dgrecki.schedulers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.entities.TicketPdfJob;
import pl.dgrecki.services.TicketGeneratorService;
import pl.dgrecki.services.TicketPdfJobService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketPdfProcessorScheduler {

    private final TicketPdfJobService ticketPdfJobService;
    private final TicketGeneratorService ticketGeneratorService;

    @Value("${scheduler.ticket-pdf.limit}")
    private int limit;

    @Scheduled(fixedDelayString = "${scheduler.ticket-pdf.delay-ms}")
    public void processPdfJobs() {
        List<TicketPdfJob> jobs = ticketPdfJobService.claimJobs(limit);
        for (TicketPdfJob job : jobs) {
            try {
                ticketGeneratorService.createTicketPdf(job);
            } catch (Exception e) {
                log.error("Failed to generate PDF for ticket {}", job.getTicketId(), e);
                ticketPdfJobService.handleFailedAttempt(job, e.getMessage());
            }
        }
    }
}
