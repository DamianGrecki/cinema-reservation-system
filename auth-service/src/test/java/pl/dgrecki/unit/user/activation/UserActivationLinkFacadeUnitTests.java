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
import pl.dgrecki.services.user.activation.ActivationLinkService;
import pl.dgrecki.services.user.activation.ActivationTokenService;
import pl.dgrecki.services.user.activation.UserActivationLinkFacade;

@ExtendWith(MockitoExtension.class)
class UserActivationLinkFacadeUnitTests {

    @Mock
    private ActivationTokenService activationTokenService;

    @Mock
    private ActivationLinkService activationLinkService;

    @InjectMocks
    private UserActivationLinkFacade facade;

    @Test
    void shouldCreateActivationLink() {
        User user = new User();
        UUID token = UUID.randomUUID();
        String expectedLink = "http://example.com/activate?token=" + token;
        ActivationToken activationToken = new ActivationToken(token, user, null);

        when(activationTokenService.createToken(user)).thenReturn(activationToken);
        when(activationLinkService.buildActivationLink(token)).thenReturn(expectedLink);

        String link = facade.createActivationLink(user);

        assertEquals(expectedLink, link);
        verify(activationTokenService, times(1)).createToken(user);
        verify(activationLinkService, times(1)).buildActivationLink(token);
    }
}
