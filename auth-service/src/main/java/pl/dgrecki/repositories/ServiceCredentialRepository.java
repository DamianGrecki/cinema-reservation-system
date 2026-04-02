package pl.dgrecki.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.ServiceCredential;

@Repository
public interface ServiceCredentialRepository extends JpaRepository<ServiceCredential, Long> {

    Optional<ServiceCredential> findByName(String name);
}
