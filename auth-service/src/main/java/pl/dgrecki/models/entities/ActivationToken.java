package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "activation_tokens")
public class ActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID token;

    @Column(nullable = false)
    private Instant expirationDate;

    @Column(nullable = false)
    private boolean isUsed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ActivationToken(UUID token, User user, Instant expirationDate) {
        this.token = token;
        this.user = user;
        this.expirationDate = expirationDate;
    }

    public void markAsUsed() {
        this.isUsed = true;
    }
}
