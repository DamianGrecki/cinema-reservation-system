package pl.dgrecki.services.user;

import static pl.dgrecki.constants.ExceptionMessages.*;
import static pl.dgrecki.constants.ValidationErrorMessages.EMAIL_ADDRESS_ALREADY_EXISTS_MSG;
import static pl.dgrecki.models.enums.UserStatus.PENDING_ACTIVATION;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.exceptions.ValidationException;
import pl.dgrecki.models.LoginResult;
import pl.dgrecki.models.entities.RefreshToken;
import pl.dgrecki.models.entities.Role;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.enums.RoleType;
import pl.dgrecki.models.enums.UserStatus;
import pl.dgrecki.models.requests.LoginRequest;
import pl.dgrecki.models.requests.UserRegisterRequest;
import pl.dgrecki.models.responses.UserRegisterResponse;
import pl.dgrecki.models.responses.UserResponse;
import pl.dgrecki.repositories.RoleRepository;
import pl.dgrecki.repositories.UserRepository;
import pl.dgrecki.services.EventService;
import pl.dgrecki.services.JwtService;
import pl.dgrecki.services.RefreshTokenService;
import pl.dgrecki.services.user.activation.UserActivationLinkFacade;
import pl.dgrecki.validators.RequestDataValidator;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EventService eventService;
    private final UserActivationLinkFacade userActivationLinkFacade;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RequestDataValidator<UserRegisterRequest> userRegisterValidator;

    @Transactional
    public UserRegisterResponse registerCustomer(UserRegisterRequest request) {
        userRegisterValidator.validate(request);
        Set<Role> customerRole = getCustomerRole();
        User savedUser = addUser(
                request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName(), customerRole);
        String activationLink = userActivationLinkFacade.createActivationLink(savedUser);
        eventService.createUserRegistrationEvent(savedUser, activationLink);
        return new UserRegisterResponse(true, savedUser.getEmail());
    }

    public LoginResult login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ValidationException(USER_NOT_ACTIVATED_MSG);
        }
        String accessToken = jwtService.generateToken(authentication, user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return new LoginResult(accessToken, refreshToken.getToken().toString());
    }

    public UserResponse getActiveUser(Long id) {
        User user = userRepository
                .findById(id)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG));
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
    }

    public void setUserStatus(UserStatus userStatus, User user) {
        user.setStatus(userStatus);
        userRepository.save(user);
    }

    private User addUser(String email, String password, String firstName, String lastName, Set<Role> roles) {
        validateEmailUniqueness(email);
        User user = new User(email, passwordEncoder.encode(password), firstName, lastName, PENDING_ACTIVATION, roles);
        return userRepository.save(user);
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException(String.format(EMAIL_ADDRESS_ALREADY_EXISTS_MSG, email));
        }
    }

    private Set<Role> getCustomerRole() {
        Role role = roleRepository
                .findByRoleType(RoleType.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND_MSG));
        return Set.of(role);
    }
}
