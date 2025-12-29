package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.USER_NOT_FOUND_MSG;
import static pl.dgrecki.models.enums.RoleType.ADMIN;
import static pl.dgrecki.models.enums.RoleType.CUSTOMER;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.Role;
import pl.dgrecki.models.User;
import pl.dgrecki.repositories.UserRepository;
import pl.dgrecki.services.CustomUserDetailsService;

class CustomUserDetailsServiceUnitTests {

    private static final String ROLE_PREFIX = "ROLE_";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsernameSuccessfullyTest() {
        String email = "test@example.com";
        String password = "password123!";
        Set<Role> roles = Set.of(new Role(ADMIN), new Role(CUSTOMER));
        User user = new User(email, password, roles);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_PREFIX + ADMIN.name())));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_PREFIX + CUSTOMER.name())));
        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsernameThrowsUserNotFoundExceptionTest() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));
        assertEquals(USER_NOT_FOUND_MSG, ex.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }
}
