package pl.dgrecki.services.user.activation;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dgrecki.models.entities.User;

@Service
@RequiredArgsConstructor
public class UserActivationLinkFacade {

    private final ActivationTokenService activationTokenService;
    private final ActivationLinkService activationLinkService;

    public String createActivationLink(User user) {
        UUID token = activationTokenService.createToken(user).getToken();
        return activationLinkService.buildActivationLink(token);
    }
}
