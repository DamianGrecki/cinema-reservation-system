package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.LOGOUT_ENDPOINT;
import static pl.dgrecki.constants.Endpoints.REFRESH_TOKEN_ENDPOINT;
import static pl.dgrecki.controllers.RefreshTokenCookieHelper.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.exceptions.ValidationException;
import pl.dgrecki.models.LoginResult;
import pl.dgrecki.models.responses.JwtTokenResponse;
import pl.dgrecki.services.RefreshTokenService;

@RestController
@RequiredArgsConstructor
class TokenController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping(REFRESH_TOKEN_ENDPOINT)
    public ResponseEntity<JwtTokenResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String tokenValue = extractRefreshToken(request)
                .orElseThrow(() -> new ValidationException("Refresh token cookie is missing"));

        LoginResult result = refreshTokenService.rotateRefreshToken(UUID.fromString(tokenValue));
        addRefreshTokenCookie(response, result.getRefreshToken());
        return ResponseEntity.ok(new JwtTokenResponse(result.getAccessToken()));
    }

    @PostMapping(LOGOUT_ENDPOINT)
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String tokenValue = extractRefreshToken(request)
                .orElseThrow(() -> new ValidationException("Refresh token cookie is missing"));
        refreshTokenService.logout(UUID.fromString(tokenValue));
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }
}
