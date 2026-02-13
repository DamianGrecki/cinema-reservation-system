package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "hall_rows")
public class HallRow {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cinema_hall_id", nullable = false, foreignKey = @ForeignKey(name = "fk_hall_rows_cinema_hall"))
    private CinemaHall cinemaHall;

    @Column(nullable = false)
    private Integer rowNumber;

    @Column(nullable = false)
    private Integer seatsCount;
}
