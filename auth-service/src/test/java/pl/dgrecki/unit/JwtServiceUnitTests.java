package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static pl.dgrecki.services.JwtService.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dgrecki.models.entities.ServiceCredential;
import pl.dgrecki.models.entities.ServiceRole;
import pl.dgrecki.models.enums.RoleType;
import pl.dgrecki.models.enums.ServiceRoleType;
import pl.dgrecki.services.JwtService;

class JwtServiceUnitTests {

    private static final String SECRET = "01234567890123456789012345678901";

    private final Clock clock = Clock.fixed(Instant.parse("2026-03-23T12:00:00Z"), ZoneId.of("Europe/Warsaw"));
    private final JwtService jwtService = new JwtService(clock);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    }

    @Test
    void generateTokenShouldReturnValidTokenTest() {
        String email = "test@example.com";
        Long userId = 1L;

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(RoleType.CUSTOMER.name()),
                new SimpleGrantedAuthority(RoleType.ADMIN.name()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);

        String token = jwtService.generateToken(authentication, userId);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = parseClaims(token);

        assertEquals(email, claims.getSubject());
        assertEquals(userId, claims.get(USER_ID, Long.class));

        List<String> roles = (List<String>) claims.get(ROLES);
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains(RoleType.CUSTOMER.name()));
        assertTrue(roles.contains(RoleType.ADMIN.name()));
        assertTrue(claims.getExpiration().after(Date.from(clock.instant())));
    }

    @Test
    void generateServiceTokenShouldReturnValidTokenTest() {
        ServiceRole role = new ServiceRole(ServiceRoleType.READ_USERS);
        ServiceCredential service = new ServiceCredential();
        ReflectionTestUtils.setField(service, "name", "cinema-service");
        ReflectionTestUtils.setField(service, "roles", Set.of(role));

        String token = jwtService.generateServiceToken(service);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = parseClaims(token);

        assertEquals("cinema-service", claims.getSubject());
        assertEquals(TOKEN_TYPE_SERVICE, claims.get(TYPE, String.class));

        List<String> roles = (List<String>) claims.get(ROLES);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(ServiceRoleType.READ_USERS.name()));
        assertTrue(claims.getExpiration().after(Date.from(clock.instant())));
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes(StandardCharsets.UTF_8))
                .setClock(() -> Date.from(clock.instant()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
