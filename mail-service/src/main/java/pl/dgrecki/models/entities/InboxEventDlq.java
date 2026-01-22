package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inbox_events_dlq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboxEventDlq {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private int partition;

    @Column(columnDefinition = "text", nullable = false)
    private String rawMessage;

    @Column(columnDefinition = "text")
    private String errorMessage;

    @Column(nullable = false)
    private Instant createdAt;
}
