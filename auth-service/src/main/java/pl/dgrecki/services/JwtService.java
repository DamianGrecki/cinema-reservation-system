package pl.dgrecki.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import pl.dgrecki.models.entities.User;

@Service
public class JwtService {

    public static final String ROLES = "roles";
    public static final String USER_ID = "userId";
    private static final Duration TOKEN_EXPIRED_TIME = Duration.ofHours(1);

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

    private String buildToken(String subject, List<String> roles, Long userId) {
        return Jwts.builder()
                .setSubject(subject)
                .claim(ROLES, roles)
                .claim(USER_ID, userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRED_TIME.toMillis()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
