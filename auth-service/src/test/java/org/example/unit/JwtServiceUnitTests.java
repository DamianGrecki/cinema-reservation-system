package org.example.unit;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.example.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceUnitTests {

    JwtService jwtService = new JwtService();

    @Test
    void generateTokenShouldReturnValidTokenTest() {
        String secret = "01234567890123456789012345678901";
        ReflectionTestUtils.setField(jwtService, "secret", secret);

        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(email, claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
    }
}
