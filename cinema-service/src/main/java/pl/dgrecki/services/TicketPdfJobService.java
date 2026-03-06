package pl.dgrecki.services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.TicketPdfJob;
import pl.dgrecki.models.enums.TicketPdfStatus;
import pl.dgrecki.repositories.TicketPdfJobRepository;

@Slf4j
@Service
public class TicketPdfJobService {

    private final TicketPdfJobRepository ticketPdfJobRepository;
    private final int maxAttempts;

    public TicketPdfJobService(
            TicketPdfJobRepository ticketPdfJobRepository,
            @Value("${scheduler.ticket-pdf.max-attempts}") int maxAttempts) {
        this.ticketPdfJobRepository = ticketPdfJobRepository;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public void createTicketPdfJob(UUID ticketId) {
        TicketPdfJob job = TicketPdfJob.builder()
                .ticketId(ticketId)
                .status(TicketPdfStatus.NEW)
                .attempts(0)
                .createdAt(Instant.now())
                .build();
        ticketPdfJobRepository.save(job);
    }

    @Transactional
    public List<TicketPdfJob> claimJobs(int limit) {
        List<TicketPdfJob> jobs = ticketPdfJobRepository.findReadyToProcess(
                TicketPdfStatus.NEW, TicketPdfStatus.FAILED, maxAttempts, PageRequest.of(0, limit));
        setJobsStatus(jobs, TicketPdfStatus.PROCESSING);
        return jobs;
    }

    public void markGenerated(TicketPdfJob job) {
        job.setStatus(TicketPdfStatus.GENERATED);
        job.setAttempts(job.getAttempts() + 1);
        job.setProcessedAt(Instant.now());
        ticketPdfJobRepository.save(job);
    }

    public void handleFailedAttempt(TicketPdfJob job, String error) {
        int attempts = job.getAttempts() + 1;
        job.setAttempts(attempts);
        job.setLastError(error);
        job.setProcessedAt(Instant.now());

        if (attempts < maxAttempts) {
            job.setStatus(TicketPdfStatus.FAILED);
        } else {
            job.setStatus(TicketPdfStatus.DEAD);
            log.warn("Ticket PDF job {} exceeded max attempts ({})", job.getId(), maxAttempts);
        }

        ticketPdfJobRepository.save(job);
    }

    public boolean areAllTicketsGeneratedForOrder(UUID orderId) {
        return ticketPdfJobRepository.countNotInStatusByOrderId(orderId, TicketPdfStatus.GENERATED) == 0;
    }

    private void setJobsStatus(List<TicketPdfJob> jobs, TicketPdfStatus status) {
        if (!jobs.isEmpty()) {
            List<UUID> ids = jobs.stream().map(TicketPdfJob::getId).toList();
            int updatedCount = ticketPdfJobRepository.setJobsStatus(ids, status);
            log.info("Status of {} ticket PDF jobs set to {}", updatedCount, status);
        }
    }
}
