package pl.dgrecki.unit.user.activation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.ActivationTokenExpiredException;
import pl.dgrecki.exceptions.ActivationTokenIsUsedException;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.ActivationToken;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.repositories.ActivationTokenRepository;
import pl.dgrecki.services.user.activation.ActivationTokenService;

@ExtendWith(MockitoExtension.class)
class ActivationTokenServiceUnitTests {

    @Mock
    private ActivationTokenRepository repository;

    @InjectMocks
    private ActivationTokenService service;

    @Test
    void shouldCreateActivationTokenTest() {
        User user = new User();
        ActivationToken activationToken = service.createToken(user);

        assertNotNull(activationToken);
        assertNotNull(activationToken.getToken());
        assertEquals(user, activationToken.getUser());

        LocalDateTime now = LocalDateTime.now();
        assertTrue(activationToken.getExpirationDate().isAfter(now.plusMinutes(59)));
        assertTrue(activationToken.getExpirationDate().isBefore(now.plusHours(2)));

        ArgumentCaptor<ActivationToken> captor = ArgumentCaptor.forClass(ActivationToken.class);
        verify(repository, times(1)).save(captor.capture());
        assertEquals(activationToken, captor.getValue());
    }

    @Test
    void shouldValidateTokenAndMarkAsUsedTest() {
        UUID token = UUID.randomUUID();
        ActivationToken activationToken =
                new ActivationToken(token, new User(), LocalDateTime.now().plusHours(1));
        assertFalse(activationToken.isUsed());

        when(repository.findByToken(token)).thenReturn(Optional.of(activationToken));

        ActivationToken validatedActivationToken = service.checkTokenAndMarkAsUsed(token);

        assertSame(activationToken, validatedActivationToken);
        assertTrue(validatedActivationToken.isUsed());
    }

    @Test
    void shouldThrowWhenTokenNotFoundTest() {
        UUID token = UUID.randomUUID();
        when(repository.findByToken(token)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> service.checkTokenAndMarkAsUsed(token));

        assertEquals(TOKEN_NOT_FOUND_MSG, ex.getMessage());
    }

    @Test
    void shouldThrowWhenTokenAlreadyUsedTest() {
        UUID token = UUID.randomUUID();

        ActivationToken activationToken =
                new ActivationToken(token, new User(), LocalDateTime.now().plusHours(1));
        activationToken.markAsUsed();

        when(repository.findByToken(token)).thenReturn(Optional.of(activationToken));

        ActivationTokenIsUsedException ex =
                assertThrows(ActivationTokenIsUsedException.class, () -> service.checkTokenAndMarkAsUsed(token));

        assertEquals(TOKEN_IS_USED_MSG, ex.getMessage());
    }

    @Test
    void shouldThrowWhenTokenExpiredTest() {
        UUID token = UUID.randomUUID();

        ActivationToken activationToken =
                new ActivationToken(token, new User(), LocalDateTime.now().minusMinutes(1));

        when(repository.findByToken(token)).thenReturn(Optional.of(activationToken));

        ActivationTokenExpiredException ex =
                assertThrows(ActivationTokenExpiredException.class, () -> service.checkTokenAndMarkAsUsed(token));

        assertEquals(TOKEN_EXPIRED_MSG, ex.getMessage());
    }
}
