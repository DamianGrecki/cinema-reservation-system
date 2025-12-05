package org.example.models.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtTokenResponse {

    private final boolean isSuccess = true;
    private final String jwtToken;
}
