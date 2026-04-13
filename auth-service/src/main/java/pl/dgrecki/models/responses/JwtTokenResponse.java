package pl.dgrecki.models.responses;

import java.time.Instant;
import lombok.Getter;

@Getter
public class JwtTokenResponse {

    private final boolean isSuccess = true;
    private final String jwtToken;
    private final Instant expiresAt;

    public JwtTokenResponse(String jwtToken) {
        this(jwtToken, null);
    }

    public JwtTokenResponse(String jwtToken, Instant expiresAt) {
        this.jwtToken = jwtToken;
        this.expiresAt = expiresAt;
    }
}
