package pl.dgrecki.services.user.activation;

import static pl.dgrecki.constants.ExceptionMessages.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.ActivationTokenExpiredException;
import pl.dgrecki.exceptions.ActivationTokenIsUsedException;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.ActivationToken;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.repositories.ActivationTokenRepository;

@Service
@RequiredArgsConstructor
public class ActivationTokenService {

    private final ActivationTokenRepository activationTokenRepository;

    private final Clock clock;

    private static final Duration VALID_DURATION = Duration.ofHours(1);

    public ActivationToken createToken(User user) {
        Instant expireDate = getExpireDate();
        ActivationToken token = new ActivationToken(UUID.randomUUID(), user, expireDate);
        activationTokenRepository.save(token);
        return token;
    }

    public ActivationToken checkTokenAndMarkAsUsed(UUID token) {
        ActivationToken activationToken = findActivationToken(token);
        if (activationToken.isUsed()) {
            throw new ActivationTokenIsUsedException(TOKEN_IS_USED_MSG);
        }
        if (activationToken.getExpirationDate().isBefore(clock.instant())) {
            throw new ActivationTokenExpiredException(TOKEN_EXPIRED_MSG);
        }
        activationToken.markAsUsed();
        return activationToken;
    }

    private ActivationToken findActivationToken(UUID token) {
        return activationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(TOKEN_NOT_FOUND_MSG));
    }

    private Instant getExpireDate() {
        return clock.instant().plus(VALID_DURATION);
    }
}
