package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "service_credentials")
public class ServiceCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID clientId;

    @Column(nullable = false)
    private String clientSecret;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private Set<ServiceRole> roles = new HashSet<>();
}
