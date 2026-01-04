package pl.dgrecki.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.requests.LoginRequest;
import pl.dgrecki.models.requests.UserRegisterRequest;
import pl.dgrecki.models.responses.JwtTokenResponse;
import pl.dgrecki.models.responses.UserRegisterResponse;
import pl.dgrecki.services.user.UserService;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
class UserController {

    private final UserService userService;

    @PostMapping("/register/customer")
    ResponseEntity<UserRegisterResponse> registerCustomer(@RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerCustomer(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.login(request));
    }
}
