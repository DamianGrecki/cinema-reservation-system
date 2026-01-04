package pl.dgrecki.services.user.activation;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.ActivationToken;
import pl.dgrecki.models.enums.UserStatus;
import pl.dgrecki.models.responses.SuccessResponse;
import pl.dgrecki.services.user.UserService;

@Service
@RequiredArgsConstructor
public class UserActivationService {

    private final UserService userService;
    private final ActivationTokenService activationTokenService;

    @Transactional
    public SuccessResponse activateUser(UUID token) {
        ActivationToken activationToken = activationTokenService.checkTokenAndMarkAsUsed(token);
        userService.setUserStatus(UserStatus.ACTIVE, activationToken.getUser());
        return new SuccessResponse();
    }
}
