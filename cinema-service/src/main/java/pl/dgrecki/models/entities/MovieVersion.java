package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.dgrecki.models.enums.AudioLanguage;
import pl.dgrecki.models.enums.MovieFormat;
import pl.dgrecki.models.enums.PresentationType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "movie_versions")
public class MovieVersion {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieFormat movieFormat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PresentationType presentationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AudioLanguage audioLanguage;

    @Column(nullable = false)
    private Instant createdAt;
}
