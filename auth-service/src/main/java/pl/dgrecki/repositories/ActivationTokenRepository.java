package pl.dgrecki.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.ActivationToken;
import pl.dgrecki.models.entities.User;

@Repository
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    List<ActivationToken> findByUser(User user);

    Optional<ActivationToken> findByToken(UUID token);
}
