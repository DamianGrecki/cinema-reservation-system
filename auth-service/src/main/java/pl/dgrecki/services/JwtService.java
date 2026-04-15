package pl.dgrecki.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import pl.dgrecki.models.entities.ServiceCredential;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.responses.JwtTokenResponse;

@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String ROLES = "roles";
    public static final String USER_ID = "userId";
    public static final String TYPE = "type";
    public static final String TOKEN_TYPE_SERVICE = "SERVICE";
    private static final Duration TOKEN_EXPIRED_TIME = Duration.ofHours(1);
    private static final Duration SERVICE_TOKEN_EXPIRED_TIME = Duration.ofMinutes(5);

    private final Clock clock;

    @Value("${jwt.secret}")
    private String secret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication, Long userId) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return buildToken(authentication.getName(), roles, userId);
    }

    public String generateTokenForUser(User user) {
        List<String> roles =
                user.getRoles().stream().map(role -> role.getRoleType().name()).toList();

        return buildToken(user.getEmail(), roles, user.getId());
    }

    public JwtTokenResponse generateServiceToken(ServiceCredential service) {
        List<String> roles = service.getRoles().stream()
                .map(role -> role.getRoleType().name())
                .toList();

        Instant now = clock.instant();
        Instant expiry = now.plus(SERVICE_TOKEN_EXPIRED_TIME);

        String token = Jwts.builder()
                .setSubject(service.getName())
                .claim(TYPE, TOKEN_TYPE_SERVICE)
                .claim(ROLES, roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        return new JwtTokenResponse(token, expiry);
    }

    private String buildToken(String subject, List<String> roles, Long userId) {
        Date now = Date.from(clock.instant());
        Date expiry = Date.from(clock.instant().plus(TOKEN_EXPIRED_TIME));

        return Jwts.builder()
                .setSubject(subject)
                .claim(ROLES, roles)
                .claim(USER_ID, userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
