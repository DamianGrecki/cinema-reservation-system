package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.USER_ACTIVATE_ENDPOINT;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.responses.SuccessResponse;
import pl.dgrecki.services.user.activation.UserActivationService;

@RestController
@RequiredArgsConstructor
public class UserActivationController {

    private final UserActivationService userActivationService;

    @GetMapping(USER_ACTIVATE_ENDPOINT)
    public ResponseEntity<SuccessResponse> activate(@RequestParam("token") UUID token) {
        return ResponseEntity.status(HttpStatus.OK).body(userActivationService.activateUser(token));
    }
}
