package pl.dgrecki.models.entities;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.dgrecki.models.enums.EventStatus;
import pl.dgrecki.models.enums.EventType;

@Entity
@Table(name = "inbox_events_dlq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboxEventDlq {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode data;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private Instant createdAt;
}
