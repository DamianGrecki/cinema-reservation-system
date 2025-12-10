package org.example.unit;

import static org.example.services.JwtService.ROLES;
import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import org.example.models.enums.RoleType;
import org.example.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceUnitTests {

    JwtService jwtService = new JwtService();

    @Test
    void generateTokenShouldReturnValidTokenTest() {
        String secret = "01234567890123456789012345678901";
        ReflectionTestUtils.setField(jwtService, "secret", secret);

        String email = "test@example.com";

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(RoleType.ROLE_CUSTOMER.name()),
                new SimpleGrantedAuthority(RoleType.ROLE_ADMIN.name()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);

        String token = jwtService.generateToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(email, claims.getSubject());

        List<String> roles = (List<String>) claims.get(ROLES);
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains(RoleType.ROLE_CUSTOMER.name()));
        assertTrue(roles.contains(RoleType.ROLE_ADMIN.name()));
        assertTrue(claims.getExpiration().after(new Date()));
    }
}
