package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

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

    @Column(name = "is_2d", nullable = false)
    private boolean is2D;

    @Column(name = "is_3d", nullable = false)
    private boolean is3D;

    @Column(nullable = false)
    private Instant createdAt;
}
