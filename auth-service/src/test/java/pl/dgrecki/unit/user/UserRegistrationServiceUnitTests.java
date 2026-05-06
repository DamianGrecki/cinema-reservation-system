package pl.dgrecki.unit.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.exceptions.ValidationException;
import pl.dgrecki.models.entities.Role;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.enums.RoleType;
import pl.dgrecki.models.requests.UserRegisterRequest;
import pl.dgrecki.models.responses.UserRegisterResponse;
import pl.dgrecki.repositories.RoleRepository;
import pl.dgrecki.repositories.UserRepository;
import pl.dgrecki.services.EventService;
import pl.dgrecki.services.user.UserRegistrationService;
import pl.dgrecki.services.user.activation.UserActivationLinkFacade;
import pl.dgrecki.validators.RequestDataValidator;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceUnitTests {

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

    private PasswordEncoder passwordEncoder;
    private UserRegistrationService userRegistrationService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userRegistrationService = new UserRegistrationService(
                userRepository,
                roleRepository,
                eventService,
                userActivationLinkFacade,
                passwordEncoder,
                userRegisterDataValidator);
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

        UserRegisterResponse response = userRegistrationService.registerCustomer(request);

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

        assertThrows(ResourceAlreadyExistsException.class, () -> userRegistrationService.registerCustomer(request));
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

        assertThrows(ValidationException.class, () -> userRegistrationService.registerCustomer(request));
        verify(userRepository, never()).save(any());
    }
}
