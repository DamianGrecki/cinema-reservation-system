package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import pl.dgrecki.models.enums.RefundReason;
import pl.dgrecki.models.enums.RefundStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "payment_attempt_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_refunds_payment_attempt"))
    private PaymentAttempt paymentAttempt;

    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    @Enumerated(EnumType.STRING)
    private RefundReason reason;

    @Column(nullable = false)
    private Instant createdAt;
}
