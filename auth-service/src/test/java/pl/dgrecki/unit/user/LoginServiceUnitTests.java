package pl.dgrecki.unit.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.USER_NOT_ACTIVATED_MSG;
import static pl.dgrecki.models.enums.UserStatus.ACTIVE;
import static pl.dgrecki.models.enums.UserStatus.PENDING_ACTIVATION;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dgrecki.exceptions.ValidationException;
import pl.dgrecki.models.LoginResult;
import pl.dgrecki.models.entities.RefreshToken;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.requests.LoginRequest;
import pl.dgrecki.repositories.UserRepository;
import pl.dgrecki.services.JwtService;
import pl.dgrecki.services.RefreshTokenService;
import pl.dgrecki.services.user.LoginService;

@ExtendWith(MockitoExtension.class)
class LoginServiceUnitTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication auth;

    @InjectMocks
    private LoginService loginService;

    @Test
    void loginUserSuccessfullyTest() {
        String email = "test@example.com";
        String password = "password123!";
        String accessToken = "mocked-jwt-token";
        Long userId = 1L;
        UUID refreshTokenUuid = UUID.randomUUID();

        LoginRequest request = new LoginRequest(email, password);

        User user = new User(email, password, "John", "Doe", ACTIVE, Set.of());
        ReflectionTestUtils.setField(user, "id", userId);

        RefreshToken refreshToken =
                RefreshToken.builder().token(refreshTokenUuid).user(user).build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(auth, userId)).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        LoginResult result = loginService.login(request);

        assertEquals(accessToken, result.getAccessToken());
        assertEquals(refreshTokenUuid.toString(), result.getRefreshToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(auth, userId);
        verify(refreshTokenService).createRefreshToken(user);
    }

    @Test
    void loginUserWhenNotActivatedShouldThrowTest() {
        String email = "test@example.com";
        String password = "password123!";

        LoginRequest request = new LoginRequest(email, password);

        User user = new User(email, password, "John", "Doe", PENDING_ACTIVATION, Set.of());
        ReflectionTestUtils.setField(user, "id", 1L);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ValidationException ex = assertThrows(ValidationException.class, () -> loginService.login(request));

        assertEquals(USER_NOT_ACTIVATED_MSG, ex.getMessage());
        verify(jwtService, never()).generateToken(any(), any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }
}
