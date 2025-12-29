package pl.dgrecki.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.dgrecki.models.enums.RoleType;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType roleType;

    public Role(RoleType roleType) {
        this.roleType = roleType;
    }
}
