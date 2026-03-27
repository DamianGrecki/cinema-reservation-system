package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID token;

    @Column(nullable = false)
    private Instant expirationDate;

    @Column(nullable = false)
    private boolean revoked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired(Clock clock) {
        return clock.instant().isAfter(expirationDate);
    }
}
