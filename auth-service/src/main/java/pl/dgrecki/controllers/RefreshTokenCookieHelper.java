package pl.dgrecki.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
class RefreshTokenCookieHelper {

    static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String COOKIE_PATH = "/api/token";
    private static final Duration MAX_AGE = Duration.ofDays(7);

    static void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = createBaseCookie(refreshToken);
        cookie.setMaxAge((int) MAX_AGE.toSeconds());
        response.addCookie(cookie);
    }

    static void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = createBaseCookie("");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    static Optional<String> extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private static Cookie createBaseCookie(String value) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // set to true in production (HTTPS)
        cookie.setPath(COOKIE_PATH);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }
}
