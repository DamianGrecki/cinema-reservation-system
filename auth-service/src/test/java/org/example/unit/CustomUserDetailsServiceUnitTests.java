package org.example.unit;

import static org.example.constants.ExceptionMessages.USER_NOT_FOUND_MSG;
import static org.example.models.enums.RoleType.ADMIN;
import static org.example.models.enums.RoleType.CUSTOMER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import org.example.exceptions.ResourceNotFoundException;
import org.example.models.Role;
import org.example.models.User;
import org.example.repositories.UserRepository;
import org.example.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
