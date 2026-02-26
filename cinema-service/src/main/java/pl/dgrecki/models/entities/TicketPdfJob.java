package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.dgrecki.models.enums.TicketPdfStatus;

@Entity
@Table(name = "ticket_pdf_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPdfJob {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID ticketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPdfStatus status;

    @Column(nullable = false)
    private int attempts;

    private String lastError;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant processedAt;
}
