package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ValidationException;
import pl.dgrecki.models.LoginResult;
import pl.dgrecki.models.entities.RefreshToken;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.repositories.RefreshTokenRepository;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final Clock clock;

    @Value("${jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID())
                .user(user)
                .expirationDate(clock.instant().plus(refreshTokenExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void logout(UUID tokenValue) {
        RefreshToken token = refreshTokenRepository
                .findByToken(tokenValue)
                .orElseThrow(() -> new ValidationException(REFRESH_TOKEN_NOT_FOUND_MSG));
        token.revoke();
    }

    @Transactional
    public LoginResult rotateRefreshToken(UUID oldTokenValue) {
        RefreshToken oldToken = refreshTokenRepository
                .findByToken(oldTokenValue)
                .orElseThrow(() -> new ValidationException(REFRESH_TOKEN_NOT_FOUND_MSG));

        if (oldToken.isRevoked()) {
            throw new ValidationException(REFRESH_TOKEN_REVOKED_MSG);
        }

        if (oldToken.isExpired(clock)) {
            oldToken.revoke();
            throw new ValidationException(REFRESH_TOKEN_EXPIRED_MSG);
        }

        oldToken.revoke();

        User user = oldToken.getUser();
        RefreshToken newRefreshToken = createRefreshToken(user);
        String accessToken = jwtService.generateTokenForUser(user);

        return new LoginResult(accessToken, newRefreshToken.getToken().toString());
    }
}
