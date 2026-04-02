package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.INTERNAL_USER_ENDPOINT;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.responses.UserResponse;
import pl.dgrecki.services.user.UserService;

@RestController
@RequiredArgsConstructor
class InternalUserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('READ_USERS')")
    @GetMapping(INTERNAL_USER_ENDPOINT)
    ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getActiveUser(id));
    }
}
