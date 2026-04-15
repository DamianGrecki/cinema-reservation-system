package pl.dgrecki.clients;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class ServiceTokenClient {

    private static final Duration REFRESH_MARGIN = Duration.ofSeconds(30);

    private final RestClient restClient;
    private final Clock clock;
    private final String name;
    private final String clientSecret;
    private final ReentrantLock lock = new ReentrantLock();

    private String cachedToken;
    private Instant tokenExpiresAt;

    public ServiceTokenClient(
            @Value("${auth-service.url}") String authServiceUrl,
            @Value("${auth-service.name}") String name,
            @Value("${auth-service.client-secret}") String clientSecret,
            Clock clock) {
        this.restClient = RestClient.builder().baseUrl(authServiceUrl).build();
        this.name = name;
        this.clientSecret = clientSecret;
        this.clock = clock;
    }

    public String getToken() {
        if (isTokenValid()) {
            return cachedToken;
        }

        lock.lock();
        try {
            if (isTokenValid()) {
                return cachedToken;
            }
            refreshToken();
            return cachedToken;
        } finally {
            lock.unlock();
        }
    }

    private boolean isTokenValid() {
        return cachedToken != null && clock.instant().isBefore(tokenExpiresAt);
    }

    private void refreshToken() {
        log.info("Requesting new service token from auth-service");
        ServiceTokenRequest request = new ServiceTokenRequest(name, clientSecret);

        ServiceTokenResponse response = restClient
                .post()
                .uri("/api/internal/token")
                .body(request)
                .retrieve()
                .body(ServiceTokenResponse.class);

        cachedToken = response.getJwtToken();
        tokenExpiresAt = response.getExpiresAt().minus(REFRESH_MARGIN);
        log.info("Service token refreshed, valid until {}", tokenExpiresAt);
    }
}
