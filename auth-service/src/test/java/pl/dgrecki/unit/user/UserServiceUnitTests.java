package pl.dgrecki.unit.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.USER_NOT_ACTIVATED_MSG;
import static pl.dgrecki.models.enums.UserStatus.ACTIVE;
import static pl.dgrecki.models.enums.UserStatus.PENDING_ACTIVATION;

import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.exceptions.ValidationException;
import pl.dgrecki.models.entities.Role;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.enums.RoleType;
import pl.dgrecki.models.enums.UserStatus;
import pl.dgrecki.models.requests.LoginRequest;
import pl.dgrecki.models.requests.UserRegisterRequest;
import pl.dgrecki.models.responses.JwtTokenResponse;
import pl.dgrecki.models.responses.UserRegisterResponse;
import pl.dgrecki.models.responses.UserResponse;
import pl.dgrecki.repositories.RoleRepository;
import pl.dgrecki.repositories.UserRepository;
import pl.dgrecki.services.EventService;
import pl.dgrecki.services.JwtService;
import pl.dgrecki.services.user.UserService;
import pl.dgrecki.services.user.activation.UserActivationLinkFacade;
import pl.dgrecki.validators.RequestDataValidator;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EventService eventService;

    @Mock
    private UserActivationLinkFacade userActivationLinkFacade;

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

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(
                userRepository,
                roleRepository,
                eventService,
                userActivationLinkFacade,
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
        Long userId = 1L;

        LoginRequest request = new LoginRequest(email, password);

        User user = new User(email, password, "John", "Doe", ACTIVE, Set.of());
        ReflectionTestUtils.setField(user, "id", userId);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(auth, userId)).thenReturn(token);

        JwtTokenResponse response = userService.login(request);

        assertEquals(token, response.getJwtToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(auth, userId);
    }

    @Test
    void loginUserWhenNotActivatedShouldThrowTest() {
        String email = "test@example.com";
        String password = "password123!";

        LoginRequest request = new LoginRequest(email, password);

        User user = new User(email, password, "John", "Doe", PENDING_ACTIVATION, Set.of());
        ReflectionTestUtils.setField(user, "id", 1L);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ValidationException ex = assertThrows(ValidationException.class, () -> userService.login(request));

        assertEquals(USER_NOT_ACTIVATED_MSG, ex.getMessage());
        verify(jwtService, never()).generateToken(any(), any());
    }

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

    @SneakyThrows
    @Test
    void registerCustomerUserSuccessfullyTest() {
        String email = "test@example.com";
        String password = "password123!";
        String firstName = "John";
        String lastName = "Doe";
        String activationLink = "https://example.com/";
        RoleType roleType = RoleType.CUSTOMER;

        UserRegisterRequest request = new UserRegisterRequest(email, password, password, firstName, lastName);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        User user = new User(email, password, firstName, lastName, PENDING_ACTIVATION, Set.of());
        when(userRepository.save(any(User.class))).thenReturn(user);

        when(roleRepository.findByRoleType(roleType)).thenReturn(Optional.of(new Role(roleType)));

        when(userActivationLinkFacade.createActivationLink(user)).thenReturn(activationLink);

        UserRegisterResponse response = userService.registerCustomer(request);

        assertTrue(response.isSuccess());
        assertEquals(email, response.getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(email, savedUser.getEmail());
        assertEquals(firstName, savedUser.getFirstName());
        assertEquals(lastName, savedUser.getLastName());
        assertNotEquals(password, savedUser.getPassword());
        assertTrue(passwordEncoder.matches(password, savedUser.getPassword()));
        assertEquals(1, savedUser.getRoles().size());
        assertTrue(savedUser.getRoles().stream().anyMatch(r -> r.getRoleType() == roleType));

        verify(eventService, times(1)).createUserRegistrationEvent(userCaptor.capture(), eq(activationLink));
    }

    @Test
    void registerCustomerUserFailsWhenEmailExistsTest() {
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        RoleType roleType = RoleType.CUSTOMER;

        UserRegisterRequest request = new UserRegisterRequest(email, password, password, firstName, lastName);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        when(roleRepository.findByRoleType(roleType)).thenReturn(Optional.of(new Role(roleType)));

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.registerCustomer(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerCustomerUserFailsWhenValidatorThrowExceptionTest() {
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";

        UserRegisterRequest request = new UserRegisterRequest(email, password, password, firstName, lastName);
        doThrow(new ValidationException("Validation failed."))
                .when(userRegisterDataValidator)
                .validate(request);

        assertThrows(ValidationException.class, () -> userService.registerCustomer(request));
        verify(userRepository, never()).save(any());
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
