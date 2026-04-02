package pl.dgrecki.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceTokenResponse {
    private boolean isSuccess;
    private String jwtToken;
}
