package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dgrecki.exceptions.ValidationException;
import pl.dgrecki.models.LoginResult;
import pl.dgrecki.models.entities.RefreshToken;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.repositories.RefreshTokenRepository;
import pl.dgrecki.services.JwtService;
import pl.dgrecki.services.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceUnitTests {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    private Clock clock;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-03-23T12:00:00Z"), ZoneId.of("UTC"));
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, jwtService, clock);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", Duration.ofDays(7));
    }

    @Test
    void shouldCreateRefreshTokenTest() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        assertNotNull(result.getToken());
        assertEquals(user, result.getUser());
        assertEquals(clock.instant().plus(Duration.ofDays(7)), result.getExpirationDate());
        assertFalse(result.isRevoked());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldRotateRefreshTokenTest() {
        UUID oldTokenUuid = UUID.randomUUID();
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        RefreshToken oldToken = RefreshToken.builder()
                .id(1L)
                .token(oldTokenUuid)
                .user(user)
                .expirationDate(clock.instant().plusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByToken(oldTokenUuid)).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateTokenForUser(user)).thenReturn("new-access-token");

        LoginResult result = refreshTokenService.rotateRefreshToken(oldTokenUuid);

        assertTrue(oldToken.isRevoked());
        assertEquals("new-access-token", result.getAccessToken());
        assertNotNull(result.getRefreshToken());

        verify(refreshTokenRepository).findByToken(oldTokenUuid);
        verify(jwtService).generateTokenForUser(user);
    }

    @Test
    void shouldThrowWhenTokenNotFoundTest() {
        UUID tokenUuid = UUID.randomUUID();
        when(refreshTokenRepository.findByToken(tokenUuid)).thenReturn(Optional.empty());

        ValidationException ex =
                assertThrows(ValidationException.class, () -> refreshTokenService.rotateRefreshToken(tokenUuid));

        assertEquals(REFRESH_TOKEN_NOT_FOUND_MSG, ex.getMessage());
    }

    @Test
    void shouldThrowWhenTokenIsRevokedTest() {
        UUID tokenUuid = UUID.randomUUID();
        RefreshToken revokedToken = RefreshToken.builder()
                .id(1L)
                .token(tokenUuid)
                .user(new User())
                .expirationDate(clock.instant().plusSeconds(3600))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken(tokenUuid)).thenReturn(Optional.of(revokedToken));

        ValidationException ex =
                assertThrows(ValidationException.class, () -> refreshTokenService.rotateRefreshToken(tokenUuid));

        assertEquals(REFRESH_TOKEN_REVOKED_MSG, ex.getMessage());
        verify(jwtService, never()).generateTokenForUser(any());
    }

    @Test
    void shouldThrowAndRevokeWhenTokenIsExpiredTest() {
        UUID tokenUuid = UUID.randomUUID();
        RefreshToken expiredToken = RefreshToken.builder()
                .id(1L)
                .token(tokenUuid)
                .user(new User())
                .expirationDate(clock.instant().minusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByToken(tokenUuid)).thenReturn(Optional.of(expiredToken));

        ValidationException ex =
                assertThrows(ValidationException.class, () -> refreshTokenService.rotateRefreshToken(tokenUuid));

        assertEquals(REFRESH_TOKEN_EXPIRED_MSG, ex.getMessage());
        assertTrue(expiredToken.isRevoked());
        verify(jwtService, never()).generateTokenForUser(any());
    }

    @Test
    void shouldLogoutAndRevokeTokenTest() {
        UUID tokenUuid = UUID.randomUUID();
        RefreshToken token = RefreshToken.builder()
                .id(1L)
                .token(tokenUuid)
                .user(new User())
                .expirationDate(clock.instant().plusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByToken(tokenUuid)).thenReturn(Optional.of(token));

        refreshTokenService.logout(tokenUuid);

        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).findByToken(tokenUuid);
    }

    @Test
    void shouldThrowWhenLogoutWithNonExistingTokenTest() {
        UUID tokenUuid = UUID.randomUUID();
        when(refreshTokenRepository.findByToken(tokenUuid)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(ValidationException.class, () -> refreshTokenService.logout(tokenUuid));

        assertEquals(REFRESH_TOKEN_NOT_FOUND_MSG, ex.getMessage());
    }
}
