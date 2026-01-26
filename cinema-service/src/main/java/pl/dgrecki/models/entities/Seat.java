package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import pl.dgrecki.models.enums.SeatType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_row_id", nullable = false, foreignKey = @ForeignKey(name = "fk_seats_hall_row"))
    private HallRow hallRow;

    @Column(nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType;
}
