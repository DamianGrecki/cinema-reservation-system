package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import pl.dgrecki.models.enums.MovieFormat;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cinema_halls")
public class CinemaHall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cinema_hall_supported_formats", joinColumns = @JoinColumn(name = "cinema_hall_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "movie_format", nullable = false)
    private Set<MovieFormat> supportedFormats = new HashSet<>();

    @Column(nullable = false)
    private Instant createdAt;
}
