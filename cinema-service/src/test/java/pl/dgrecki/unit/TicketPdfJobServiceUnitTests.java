package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import pl.dgrecki.models.entities.TicketPdfJob;
import pl.dgrecki.models.enums.TicketPdfStatus;
import pl.dgrecki.repositories.TicketPdfJobRepository;
import pl.dgrecki.services.TicketPdfJobService;

@ExtendWith(MockitoExtension.class)
class TicketPdfJobServiceUnitTests {

    @Mock
    private TicketPdfJobRepository ticketPdfJobRepository;

    private static final int MAX_ATTEMPTS = 3;

    private TicketPdfJobService ticketPdfJobService;

    @BeforeEach
    void setUp() {
        ticketPdfJobService = new TicketPdfJobService(ticketPdfJobRepository, MAX_ATTEMPTS);
    }

    @Test
    void createTicketPdfJobShouldSaveJobWithCorrectFieldsTest() {
        UUID ticketId = UUID.randomUUID();

        ticketPdfJobService.createTicketPdfJob(ticketId);

        ArgumentCaptor<TicketPdfJob> captor = ArgumentCaptor.forClass(TicketPdfJob.class);
        verify(ticketPdfJobRepository).save(captor.capture());

        TicketPdfJob saved = captor.getValue();
        assertEquals(ticketId, saved.getTicketId());
        assertEquals(TicketPdfStatus.NEW, saved.getStatus());
        assertEquals(0, saved.getAttempts());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void claimJobsShouldReturnJobsAndSetStatusToProcessingTest() {
        int limit = 10;
        UUID jobId = UUID.randomUUID();
        TicketPdfJob job = TicketPdfJob.builder()
                .id(jobId)
                .status(TicketPdfStatus.NEW)
                .attempts(0)
                .build();

        when(ticketPdfJobRepository.findReadyToProcess(
                        TicketPdfStatus.NEW, TicketPdfStatus.FAILED, MAX_ATTEMPTS, PageRequest.of(0, limit)))
                .thenReturn(List.of(job));
        when(ticketPdfJobRepository.setJobsStatus(List.of(jobId), TicketPdfStatus.PROCESSING))
                .thenReturn(1);

        List<TicketPdfJob> result = ticketPdfJobService.claimJobs(limit);

        assertEquals(1, result.size());
        verify(ticketPdfJobRepository).setJobsStatus(List.of(jobId), TicketPdfStatus.PROCESSING);
    }

    @Test
    void claimJobsWhenNoJobsFoundShouldNotCallSetJobsStatusTest() {
        int limit = 10;

        when(ticketPdfJobRepository.findReadyToProcess(
                        TicketPdfStatus.NEW, TicketPdfStatus.FAILED, MAX_ATTEMPTS, PageRequest.of(0, limit)))
                .thenReturn(List.of());

        List<TicketPdfJob> result = ticketPdfJobService.claimJobs(limit);

        assertTrue(result.isEmpty());
        verify(ticketPdfJobRepository, never()).setJobsStatus(any(), any());
    }

    @Test
    void markGeneratedShouldSetCorrectStatusAndIncrementAttemptsTest() {
        TicketPdfJob job = TicketPdfJob.builder()
                .id(UUID.randomUUID())
                .status(TicketPdfStatus.PROCESSING)
                .attempts(1)
                .build();

        ticketPdfJobService.markGenerated(job);

        assertEquals(TicketPdfStatus.GENERATED, job.getStatus());
        assertEquals(2, job.getAttempts());
        assertNotNull(job.getProcessedAt());
        verify(ticketPdfJobRepository).save(job);
    }

    @Test
    void handleFailedAttemptBelowMaxAttemptsShouldSetStatusToFailedTest() {
        TicketPdfJob job = TicketPdfJob.builder()
                .id(UUID.randomUUID())
                .status(TicketPdfStatus.PROCESSING)
                .attempts(1)
                .build();

        ticketPdfJobService.handleFailedAttempt(job, "PDF generation failed");

        assertEquals(TicketPdfStatus.FAILED, job.getStatus());
        assertEquals(2, job.getAttempts());
        assertEquals("PDF generation failed", job.getLastError());
        assertNotNull(job.getProcessedAt());
        verify(ticketPdfJobRepository).save(job);
    }

    @Test
    void handleFailedAttemptAtMaxAttemptsShouldSetStatusToDeadTest() {
        TicketPdfJob job = TicketPdfJob.builder()
                .id(UUID.randomUUID())
                .status(TicketPdfStatus.PROCESSING)
                .attempts(MAX_ATTEMPTS - 1)
                .build();

        ticketPdfJobService.handleFailedAttempt(job, "PDF generation failed");

        assertEquals(TicketPdfStatus.DEAD, job.getStatus());
        assertEquals(MAX_ATTEMPTS, job.getAttempts());
        verify(ticketPdfJobRepository).save(job);
    }
}
