package org.example.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import org.example.models.Role;
import org.example.models.User;
import org.example.models.enums.RoleType;
import org.example.repositories.UserRepository;
import org.example.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

class CustomUserDetailsServiceUnitTests {

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
        Set<Role> roles = Set.of(new Role(RoleType.ROLE_ADMIN), new Role(RoleType.ROLE_CUSTOMER));
        User user = new User(email, password, roles);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(RoleType.ROLE_ADMIN.name())));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(RoleType.ROLE_CUSTOMER.name())));
        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsernameThrowsBadCredentialsExceptionTest() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> userDetailsService.loadUserByUsername(email));
        verify(userRepository).findByEmail(email);
    }
}
