package pl.dgrecki.unit.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.models.enums.UserStatus.ACTIVE;
import static pl.dgrecki.models.enums.UserStatus.PENDING_ACTIVATION;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.enums.UserStatus;
import pl.dgrecki.models.responses.UserResponse;
import pl.dgrecki.repositories.UserRepository;
import pl.dgrecki.services.user.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getActiveUserSuccessfullyTest() {
        Long userId = 1L;
        User user = new User("test@example.com", "pass", "John", "Doe", ACTIVE, Set.of());
        ReflectionTestUtils.setField(user, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getActiveUser(userId);

        assertEquals(userId, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void getActiveUserWhenNotActiveShouldThrowTest() {
        Long userId = 1L;
        User user = new User("test@example.com", "pass", "John", "Doe", PENDING_ACTIVATION, Set.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> userService.getActiveUser(userId));
    }

    @Test
    void shouldSetUserStatusAndSaveUserTest() {
        User user = new User();
        user.setStatus(UserStatus.PENDING_ACTIVATION);

        userService.setUserStatus(UserStatus.ACTIVE, user);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userRepository, times(1)).save(user);
    }
}
