package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.LOGIN_ENDPOINT;
import static pl.dgrecki.constants.Endpoints.REGISTER_CUSTOMER_ENDPOINT;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.requests.LoginRequest;
import pl.dgrecki.models.requests.UserRegisterRequest;
import pl.dgrecki.models.responses.JwtTokenResponse;
import pl.dgrecki.models.responses.UserRegisterResponse;
import pl.dgrecki.services.user.UserService;

@RestController
@AllArgsConstructor
class UserController {

    private final UserService userService;

    @PostMapping(REGISTER_CUSTOMER_ENDPOINT)
    ResponseEntity<UserRegisterResponse> registerCustomer(@RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerCustomer(request));
    }

    @PostMapping(LOGIN_ENDPOINT)
    public ResponseEntity<JwtTokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.login(request));
    }
}
