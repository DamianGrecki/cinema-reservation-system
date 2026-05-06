package pl.dgrecki.integration;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.dgrecki.constants.Endpoints.*;

import jakarta.servlet.http.Cookie;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.dgrecki.models.entities.RefreshToken;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.enums.UserStatus;
import pl.dgrecki.models.requests.UserRegisterRequest;
import pl.dgrecki.repositories.RefreshTokenRepository;
import pl.dgrecki.repositories.UserRepository;
import pl.dgrecki.services.RefreshTokenService;
import pl.dgrecki.services.user.UserRegistrationService;

@SpringBootTest
@AutoConfigureMockMvc
class TokenControllerIntegrationTests extends BaseIntegrationTest {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @SneakyThrows
    @Test
    void shouldRefreshTokenSuccessfullyTest() {
        User user = registerAndActivateUser();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        MvcResult result = mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
                        .cookie(new Cookie(
                                REFRESH_TOKEN_COOKIE, refreshToken.getToken().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken", notNullValue()))
                .andReturn();

        Cookie newCookie = result.getResponse().getCookie(REFRESH_TOKEN_COOKIE);
        assertNotNull(newCookie);
        assertNotEquals(refreshToken.getToken().toString(), newCookie.getValue());

        RefreshToken oldToken =
                refreshTokenRepository.findByToken(refreshToken.getToken()).orElseThrow();
        assertTrue(oldToken.isRevoked());
    }

    @SneakyThrows
    @Test
    void shouldReturn400WhenRefreshTokenCookieIsMissingTest() {
        mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)).andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void shouldReturn400WhenRefreshTokenIsRevokedTest() {
        User user = registerAndActivateUser();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
                        .cookie(new Cookie(
                                REFRESH_TOKEN_COOKIE, refreshToken.getToken().toString())))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void shouldLogoutAndRevokeTokenTest() {
        User user = registerAndActivateUser();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        MvcResult result = mockMvc.perform(post(LOGOUT_ENDPOINT)
                        .cookie(new Cookie(
                                REFRESH_TOKEN_COOKIE, refreshToken.getToken().toString())))
                .andExpect(status().isNoContent())
                .andReturn();

        Cookie clearedCookie = result.getResponse().getCookie(REFRESH_TOKEN_COOKIE);
        assertNotNull(clearedCookie);
        assertEquals(0, clearedCookie.getMaxAge());

        RefreshToken revokedToken =
                refreshTokenRepository.findByToken(refreshToken.getToken()).orElseThrow();
        assertTrue(revokedToken.isRevoked());
    }

    @SneakyThrows
    @Test
    void shouldReturn400WhenLogoutWithoutCookieTest() {
        mockMvc.perform(post(LOGOUT_ENDPOINT)).andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void shouldReturnRefreshTokenCookieOnLoginTest() {
        registerAndActivateUser();
        MvcResult result = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken", notNullValue()))
                .andReturn();

        Cookie refreshCookie = result.getResponse().getCookie(REFRESH_TOKEN_COOKIE);
        assertNotNull(refreshCookie);
        assertTrue(refreshCookie.getMaxAge() > 0);
    }

    private User registerAndActivateUser() {
        UserRegisterRequest request =
                new UserRegisterRequest("test@example.com", "Password123!", "Password123!", "John", "Doe");
        userRegistrationService.registerCustomer(request);
        User user = userRepository.findByEmail("test@example.com").orElseThrow();
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }
}
