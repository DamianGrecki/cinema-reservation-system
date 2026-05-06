package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.LOGIN_ENDPOINT;
import static pl.dgrecki.constants.Endpoints.REGISTER_CUSTOMER_ENDPOINT;
import static pl.dgrecki.controllers.RefreshTokenCookieHelper.addRefreshTokenCookie;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.LoginResult;
import pl.dgrecki.models.requests.LoginRequest;
import pl.dgrecki.models.requests.UserRegisterRequest;
import pl.dgrecki.models.responses.JwtTokenResponse;
import pl.dgrecki.models.responses.UserRegisterResponse;
import pl.dgrecki.services.user.LoginService;
import pl.dgrecki.services.user.UserRegistrationService;

@RestController
@AllArgsConstructor
class UserController {

    private final UserRegistrationService userRegistrationService;
    private final LoginService loginService;

    @PostMapping(REGISTER_CUSTOMER_ENDPOINT)
    ResponseEntity<UserRegisterResponse> registerCustomer(@RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRegistrationService.registerCustomer(request));
    }

    @PostMapping(LOGIN_ENDPOINT)
    public ResponseEntity<JwtTokenResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResult result = loginService.login(request);
        addRefreshTokenCookie(response, result.getRefreshToken());
        return ResponseEntity.ok(new JwtTokenResponse(result.getAccessToken()));
    }
}
