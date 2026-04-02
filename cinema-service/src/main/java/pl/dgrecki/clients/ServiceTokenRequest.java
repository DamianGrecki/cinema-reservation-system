package pl.dgrecki.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceTokenRequest {
    private final String name;
    private final String clientSecret;
}
