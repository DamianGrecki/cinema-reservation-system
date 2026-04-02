package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.INTERNAL_TOKEN_ENDPOINT;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.requests.ServiceAuthRequest;
import pl.dgrecki.models.responses.JwtTokenResponse;
import pl.dgrecki.services.ServiceAuthService;

@RestController
@RequiredArgsConstructor
class ServiceAuthController {

    private final ServiceAuthService serviceAuthService;

    @PostMapping(INTERNAL_TOKEN_ENDPOINT)
    ResponseEntity<JwtTokenResponse> authenticate(@RequestBody ServiceAuthRequest request) {
        String token = serviceAuthService.authenticate(request.getName(), request.getClientSecret());
        return ResponseEntity.ok(new JwtTokenResponse(token));
    }
}
