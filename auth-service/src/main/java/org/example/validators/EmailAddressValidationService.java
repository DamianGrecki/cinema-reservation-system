package org.example.validators;

import static org.example.constants.EmailAddressConstraints.MAX_EMAIL_ADDRESS_LENGTH;
import static org.example.constants.EmailAddressConstraints.MIN_EMAIL_ADDRESS_LENGTH;
import static org.example.constants.ValidationErrorMessages.*;

import java.util.List;
import org.example.exceptions.ValidationsException;
import org.springframework.stereotype.Service;

@Service
public class EmailAddressValidationService {

    public void validateEmail(String email) {
        validateEmailPresence(email);
        Validator validator = Validator.link(
                new StringLengthValidator(
                        MIN_EMAIL_ADDRESS_LENGTH,
                        MAX_EMAIL_ADDRESS_LENGTH,
                        MIN_EMAIL_ADDRESS_LENGTH_MSG,
                        MAX_EMAIL_ADDRESS_LENGTH_MSG),
                new EmailAddressFormatValidator());
        List<String> errors = validator.validate(email);
        if (!errors.isEmpty()) {
            throw new ValidationsException(errors);
        }
    }

    private void validateEmailPresence(String email) {
        if (email == null || email.isEmpty()) {
            throw new ValidationsException(List.of(EMAIL_ADDRESS_IS_REQUIRED_MSG));
        }
    }
}
