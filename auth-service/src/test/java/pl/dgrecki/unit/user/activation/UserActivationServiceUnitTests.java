package pl.dgrecki.unit.user.activation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.models.entities.ActivationToken;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.enums.UserStatus;
import pl.dgrecki.models.responses.SuccessResponse;
import pl.dgrecki.services.user.UserService;
import pl.dgrecki.services.user.activation.ActivationTokenService;
import pl.dgrecki.services.user.activation.UserActivationService;

@ExtendWith(MockitoExtension.class)
class UserActivationServiceUnitTests {

    @Mock
    private UserService userService;

    @Mock
    private ActivationTokenService activationTokenService;

    @InjectMocks
    private UserActivationService userActivationService;

    @Test
    void shouldActivateUserTest() {
        UUID token = UUID.randomUUID();
        User user = new User();
        ActivationToken activationToken = new ActivationToken(token, user, null);

        when(activationTokenService.checkTokenAndMarkAsUsed(token)).thenReturn(activationToken);

        SuccessResponse response = userActivationService.activateUser(token);

        assertNotNull(response);
        verify(activationTokenService, times(1)).checkTokenAndMarkAsUsed(token);
        verify(userService, times(1)).setUserStatus(UserStatus.ACTIVE, user);
    }

    @Test
    void shouldNotActivateWhenTokenIsInvalidTest() {
        UUID token = UUID.randomUUID();

        RuntimeException ex = new RuntimeException("error");
        when(activationTokenService.checkTokenAndMarkAsUsed(token)).thenThrow(ex);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userActivationService.activateUser(token));

        assertSame(ex, thrown);

        verify(userService, never()).setUserStatus(any(), any());
    }
}
