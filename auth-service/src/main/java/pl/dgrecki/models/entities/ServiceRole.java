package pl.dgrecki.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.dgrecki.models.enums.ServiceRoleType;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "service_roles")
public class ServiceRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRoleType roleType;

    public ServiceRole(ServiceRoleType roleType) {
        this.roleType = roleType;
    }
}
