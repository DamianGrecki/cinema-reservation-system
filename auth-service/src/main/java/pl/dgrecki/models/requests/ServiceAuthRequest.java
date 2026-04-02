package pl.dgrecki.models.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceAuthRequest {
    private final String name;
    private final String clientSecret;
}
