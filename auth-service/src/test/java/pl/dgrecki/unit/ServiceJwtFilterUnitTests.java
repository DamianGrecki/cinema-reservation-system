package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dgrecki.config.ServiceJwtFilter;
import pl.dgrecki.services.JwtService;

@ExtendWith(MockitoExtension.class)
class ServiceJwtFilterUnitTests {

    private static final String SECRET = "01234567890123456789012345678901";
    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private ServiceJwtFilter serviceJwtFilter;

    @BeforeEach
    void setUp() {
        serviceJwtFilter = new ServiceJwtFilter();
        ReflectionTestUtils.setField(serviceJwtFilter, "secret", SECRET);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @SneakyThrows
    @Test
    void shouldAuthenticateWithValidServiceTokenTest() {
        String token = buildServiceToken("cinema-service", List.of("READ_USERS"));
        when(request.getHeader(SERVICE_TOKEN_HEADER)).thenReturn(token);

        serviceJwtFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(
                "cinema-service",
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("READ_USERS")));
        verify(filterChain).doFilter(request, response);
    }

    @SneakyThrows
    @Test
    void shouldContinueFilterChainWithoutTokenTest() {
        when(request.getHeader(SERVICE_TOKEN_HEADER)).thenReturn(null);

        serviceJwtFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @SneakyThrows
    @Test
    void shouldClearContextWithInvalidTokenTest() {
        when(request.getHeader(SERVICE_TOKEN_HEADER)).thenReturn("invalid-token");

        serviceJwtFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @SneakyThrows
    @Test
    void shouldClearContextWhenTokenTypeIsNotServiceTest() {
        String token = buildTokenWithType("cinema-service", "USER");
        when(request.getHeader(SERVICE_TOKEN_HEADER)).thenReturn(token);

        serviceJwtFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @SneakyThrows
    @Test
    void shouldClearContextWithExpiredTokenTest() {
        Instant past = Instant.now().minus(Duration.ofHours(1));
        String token = Jwts.builder()
                .setSubject("cinema-service")
                .claim(JwtService.TYPE, JwtService.TOKEN_TYPE_SERVICE)
                .claim(JwtService.ROLES, List.of("READ_USERS"))
                .setIssuedAt(Date.from(past.minus(Duration.ofHours(1))))
                .setExpiration(Date.from(past))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        when(request.getHeader(SERVICE_TOKEN_HEADER)).thenReturn(token);

        serviceJwtFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    private String buildServiceToken(String serviceName, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(serviceName)
                .claim(JwtService.TYPE, JwtService.TOKEN_TYPE_SERVICE)
                .claim(JwtService.ROLES, roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(Duration.ofMinutes(5))))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    private String buildTokenWithType(String serviceName, String type) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(serviceName)
                .claim(JwtService.TYPE, type)
                .claim(JwtService.ROLES, List.of("READ_USERS"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(Duration.ofMinutes(5))))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
}
