package pl.dgrecki.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.dgrecki.exceptions.ResourceNotFoundException;

@Slf4j
@Component
public class AuthServiceClient {

    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";

    private final RestClient restClient;
    private final ServiceTokenClient serviceTokenClient;

    public AuthServiceClient(
            @Value("${auth-service.url}") String authServiceUrl, ServiceTokenClient serviceTokenClient) {
        this.restClient = RestClient.builder().baseUrl(authServiceUrl).build();
        this.serviceTokenClient = serviceTokenClient;
    }

    public AuthUserResponse getUser(Long userId) {
        log.info("Fetching user {} from auth-service", userId);
        AuthUserResponse response = restClient
                .get()
                .uri("/api/internal/users/{id}", userId)
                .header(SERVICE_TOKEN_HEADER, serviceTokenClient.getToken())
                .retrieve()
                .body(AuthUserResponse.class);

        if (response == null) {
            throw new ResourceNotFoundException("User %d not found in auth-service".formatted(userId));
        }
        return response;
    }
}
