package pl.dgrecki.services.user;

import static pl.dgrecki.constants.ExceptionMessages.USER_NOT_ACTIVATED_MSG;
import static pl.dgrecki.constants.ExceptionMessages.USER_NOT_FOUND_MSG;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.exceptions.ValidationException;
import pl.dgrecki.models.LoginResult;
import pl.dgrecki.models.entities.RefreshToken;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.enums.UserStatus;
import pl.dgrecki.models.requests.LoginRequest;
import pl.dgrecki.repositories.UserRepository;
import pl.dgrecki.services.JwtService;
import pl.dgrecki.services.RefreshTokenService;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public LoginResult login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ValidationException(USER_NOT_ACTIVATED_MSG);
        }
        String accessToken = jwtService.generateToken(authentication, user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return new LoginResult(accessToken, refreshToken.getToken().toString());
    }
}
