package org.example.services;

import static org.example.constants.ExceptionMessages.ROLE_NOT_FOUND_MSG;
import static org.example.constants.ValidationErrorMessages.EMAIL_ADDRESS_ALREADY_EXISTS_MSG;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.exceptions.ResourceAlreadyExistsException;
import org.example.exceptions.ResourceNotFoundException;
import org.example.models.Role;
import org.example.models.User;
import org.example.models.enums.RoleType;
import org.example.models.requests.LoginRequest;
import org.example.models.requests.UserRegisterRequest;
import org.example.models.responses.JwtTokenResponse;
import org.example.models.responses.UserRegisterResponse;
import org.example.repositories.RoleRepository;
import org.example.repositories.UserRepository;
import org.example.validators.RequestDataValidator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EventService eventService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RequestDataValidator<UserRegisterRequest> userRegisterValidator;

    @SneakyThrows
    @Transactional
    public UserRegisterResponse registerCustomer(UserRegisterRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        userRegisterValidator.validate(request);
        Set<Role> customerRole = getCustomerRole();
        User savedUser = addUser(email, password, customerRole);
        eventService.createUserRegistrationMailEvent(savedUser);
        return new UserRegisterResponse(true, savedUser.getEmail());
    }

    public JwtTokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        String token = jwtService.generateToken(authentication);
        return new JwtTokenResponse(token);
    }

    private User addUser(String email, String password, Set<Role> roles) {
        validateEmailUniqueness(email);
        User user = new User(email, passwordEncoder.encode(password), roles);
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
