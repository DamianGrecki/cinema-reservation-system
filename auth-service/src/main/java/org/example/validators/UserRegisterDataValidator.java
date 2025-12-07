package org.example.validators;

import lombok.RequiredArgsConstructor;
import org.example.models.requests.UserRegisterRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisterDataValidator implements RequestDataValidator<UserRegisterRequest> {

    private final EmailAddressValidationService emailService;
    private final PasswordValidationService passwordService;

    @Override
    public void validate(UserRegisterRequest request) {
        emailService.validateEmail(request.getEmail());
        passwordService.validatePassword(request.getPassword());
        passwordService.comparePasswords(request.getPassword(), request.getConfirmPassword());
    }
}
