package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.exceptions.ResourceAlreadyExistsException;
import org.example.models.User;
import org.example.models.requests.UserRegisterRequestBody;
import org.example.models.responses.UserRegisterResponse;
import org.example.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.constants.Messages.EMAIL_ADDRESS_ALREADY_EXISTS_MSG;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordValidationService passwordValidationService;
    private final EmailAddressValidationService emailValidationService;

    @Transactional
    public UserRegisterResponse register(UserRegisterRequestBody request) {
        String email = request.getEmail();
        String password = request.getPassword();
        String confirmedPassword = request.getConfirmPassword();
        emailValidationService.validateEmail(email);
        passwordValidationService.validatePassword(password);
        passwordValidationService.comparePasswords(password, confirmedPassword);
        addUser(email, password);
        return new UserRegisterResponse(true, email);
    }

    private void addUser(String email, String password) {
        validateEmailUniqueness(email);
        User user = new User(
                email,
                passwordValidationService.encodePassword(password)
        );
        userRepository.save(user);
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException(
                    String.format(EMAIL_ADDRESS_ALREADY_EXISTS_MSG, email)
            );
        }
    }

}