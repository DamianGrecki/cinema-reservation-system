package pl.dgrecki.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.dgrecki.services.JwtService;

@Slf4j
@Component
public class ServiceJwtFilter extends OncePerRequestFilter {

    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";

    @Value("${jwt.secret}")
    private String secret;

    @SneakyThrows
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) {

        extractToken(request).ifPresent(token -> {
            try {
                Claims claims = parseClaims(token);
                String type = claims.get(JwtService.TYPE, String.class);

                if (!JwtService.TOKEN_TYPE_SERVICE.equals(type)) {
                    throw new JwtException("Invalid token type");
                }

                Authentication auth = buildAuthentication(claims);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                log.warn("Invalid service JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        });

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String token = request.getHeader(SERVICE_TOKEN_HEADER);
        return Optional.ofNullable(token);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Authentication buildAuthentication(Claims claims) {
        String serviceName = claims.getSubject();
        List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
        return new UsernamePasswordAuthenticationToken(serviceName, null, authorities);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<String> roles = ((List<?>) claims.get(JwtService.ROLES))
                .stream().map(Object::toString).toList();

        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }
}
