package org.example.services;

import static org.example.constants.Messages.EMAIL_ADDRESS_ALREADY_EXISTS_MSG;

import lombok.RequiredArgsConstructor;
import org.example.exceptions.ResourceAlreadyExistsException;
import org.example.models.User;
import org.example.models.requests.LoginRequest;
import org.example.models.requests.UserRegisterRequest;
import org.example.models.responses.JwtTokenResponse;
import org.example.models.responses.UserRegisterResponse;
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
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RequestDataValidator<UserRegisterRequest> userRegisterValidator;

    @Transactional
    public UserRegisterResponse register(UserRegisterRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        userRegisterValidator.validate(request);
        addUser(email, password);
        return new UserRegisterResponse(true, email);
    }

    public JwtTokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        String token = jwtService.generateToken(authentication.getName());
        return new JwtTokenResponse(token);
    }

    private void addUser(String email, String password) {
        validateEmailUniqueness(email);
        User user = new User(email, passwordEncoder.encode(password));
        userRepository.save(user);
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException(String.format(EMAIL_ADDRESS_ALREADY_EXISTS_MSG, email));
        }
    }
}
