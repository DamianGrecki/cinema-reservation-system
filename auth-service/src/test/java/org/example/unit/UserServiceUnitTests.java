package org.example.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.exceptions.ResourceAlreadyExistsException;
import org.example.exceptions.ValidationException;
import org.example.exceptions.ValidationsException;
import org.example.models.User;
import org.example.models.requests.LoginRequest;
import org.example.models.requests.UserRegisterRequest;
import org.example.models.responses.JwtTokenResponse;
import org.example.models.responses.UserRegisterResponse;
import org.example.repositories.UserRepository;
import org.example.services.EmailAddressValidationService;
import org.example.services.JwtService;
import org.example.services.PasswordValidationService;
import org.example.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
class UserServiceUnitTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordValidationService passwordValidationService;

    @Mock
    private EmailAddressValidationService emailValidationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication auth;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(
                userRepository,
                passwordValidationService,
                emailValidationService,
                passwordEncoder,
                authenticationManager,
                jwtService);
    }

    @Test
    void loginUserSuccessfullyTest() {
        String email = "test@example.com";
        String password = "password123!";

        String token = "mocked-jwt-token";

        LoginRequest request = new LoginRequest(email, password);
        when(auth.getName()).thenReturn(email);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtService.generateToken(email)).thenReturn(token);

        JwtTokenResponse response = userService.login(request);

        assertEquals(token, response.getJwtToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(email);
    }

    @Test
    void registerUserSuccessfullyTest() {
        String email = "test@example.com";
        String password = "password123!";

        UserRegisterRequest request = new UserRegisterRequest(email, password, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserRegisterResponse response = userService.register(request);

        assertTrue(response.isSuccess());
        assertEquals(email, response.getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(email, savedUser.getEmail());
        assertNotEquals(password, savedUser.getPassword());
        assertTrue(passwordEncoder.matches(password, savedUser.getPassword()));
    }

    @Test
    void registerUserFailsWhenEmailExistsTest() {
        String email = "test@example.com";
        String password = "Password123!";

        UserRegisterRequest request = new UserRegisterRequest(email, password, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        assertThrows(ResourceAlreadyExistsException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUserFailsWhenPasswordsDoNotMatchTest() {
        String email = "test@example.com";
        String password = "Password123!";
        String confirmedPassword = "Password123!4";

        UserRegisterRequest request = new UserRegisterRequest(email, password, confirmedPassword);
        doThrow(new ValidationException("Passwords do not match."))
                .when(passwordValidationService)
                .comparePasswords(password, confirmedPassword);

        assertThrows(ValidationException.class, () -> userService.register(request));

        verify(passwordValidationService, times(1)).comparePasswords(password, confirmedPassword);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUserFailsWhenPasswordIsInValidTest() {
        String email = "test@example.com";
        String password = "Password";

        UserRegisterRequest request = new UserRegisterRequest(email, password, password);
        doThrow(new ValidationsException(new ArrayList<>()))
                .when(passwordValidationService)
                .validatePassword(password);

        assertThrows(ValidationsException.class, () -> userService.register(request));

        verify(passwordValidationService, times(1)).validatePassword(password);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUserFailsWhenEmailAddressIsInValidTest() {
        String email = "testexample.com";
        String password = "Password";

        UserRegisterRequest request = new UserRegisterRequest(email, password, password);
        doThrow(new ValidationsException(new ArrayList<>()))
                .when(emailValidationService)
                .validateEmail(email);

        assertThrows(ValidationsException.class, () -> userService.register(request));

        verify(emailValidationService, times(1)).validateEmail(email);
        verify(userRepository, never()).save(any());
    }
}
