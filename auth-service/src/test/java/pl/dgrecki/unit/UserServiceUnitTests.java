package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.exceptions.ValidationException;
import pl.dgrecki.models.Role;
import pl.dgrecki.models.User;
import pl.dgrecki.models.enums.RoleType;
import pl.dgrecki.models.requests.LoginRequest;
import pl.dgrecki.models.requests.UserRegisterRequest;
import pl.dgrecki.models.responses.JwtTokenResponse;
import pl.dgrecki.models.responses.UserRegisterResponse;
import pl.dgrecki.repositories.RoleRepository;
import pl.dgrecki.repositories.UserRepository;
import pl.dgrecki.services.EventService;
import pl.dgrecki.services.JwtService;
import pl.dgrecki.services.UserService;
import pl.dgrecki.validators.RequestDataValidator;

@RequiredArgsConstructor
class UserServiceUnitTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EventService eventService;

    @Mock
    private RequestDataValidator<UserRegisterRequest> userRegisterDataValidator;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication auth;

    private PasswordEncoder passwordEncoder;
    private UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(
                userRepository,
                roleRepository,
                eventService,
                passwordEncoder,
                authenticationManager,
                jwtService,
                userRegisterDataValidator);
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
        when(jwtService.generateToken(any(Authentication.class))).thenReturn(token);

        JwtTokenResponse response = userService.login(request);

        assertEquals(token, response.getJwtToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(any(Authentication.class));
    }

    @SneakyThrows
    @Test
    void registerCustomerUserSuccessfullyTest() {
        String email = "test@example.com";
        String password = "password123!";
        RoleType roleType = RoleType.CUSTOMER;

        UserRegisterRequest request = new UserRegisterRequest(email, password, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenReturn(new User(email, password, Set.of()))
                .getMock();

        when(roleRepository.findByRoleType(roleType)).thenReturn(Optional.of(new Role(roleType)));

        UserRegisterResponse response = userService.registerCustomer(request);

        assertTrue(response.isSuccess());
        assertEquals(email, response.getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(email, savedUser.getEmail());
        assertNotEquals(password, savedUser.getPassword());
        assertTrue(passwordEncoder.matches(password, savedUser.getPassword()));
        assertEquals(1, savedUser.getRoles().size());
        assertTrue(savedUser.getRoles().stream().anyMatch(r -> r.getRoleType() == roleType));

        verify(eventService, times(1)).createUserRegistrationMailEvent(userCaptor.capture());
    }

    @Test
    void registerCustomerUserFailsWhenEmailExistsTest() {
        String email = "test@example.com";
        String password = "Password123!";
        RoleType roleType = RoleType.CUSTOMER;

        UserRegisterRequest request = new UserRegisterRequest(email, password, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        when(roleRepository.findByRoleType(roleType)).thenReturn(Optional.of(new Role(roleType)));

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.registerCustomer(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerCustomerUserFailsWhenValidatorThrowExceptionTest() {
        String email = "test@example.com";
        String password = "Password123!";

        UserRegisterRequest request = new UserRegisterRequest(email, password, password);
        doThrow(new ValidationException("Validation failed."))
                .when(userRegisterDataValidator)
                .validate(request);

        assertThrows(ValidationException.class, () -> userService.registerCustomer(request));
        verify(userRepository, never()).save(any());
    }
}
