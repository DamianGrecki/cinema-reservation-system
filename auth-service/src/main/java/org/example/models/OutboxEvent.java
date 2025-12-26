package org.example.models;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "outbox_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue
    private UUID id;

    private Long aggregateId;

    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private Instant createdAt;
    private Instant sentAt;

    public enum Status {
        PENDING,
        SENT,
        FAILED
    }

    public enum EventType {
        USER_REGISTERED_MAIL
    }

    public enum AggregateType {
        USER
    }
}
