package pl.dgrecki.validators;

import static pl.dgrecki.constants.PasswordConstraints.MAX_PASSWORD_LENGTH;
import static pl.dgrecki.constants.PasswordConstraints.MIN_PASSWORD_LENGTH;
import static pl.dgrecki.constants.ValidationErrorMessages.*;

import java.util.List;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.ValidationException;
import pl.dgrecki.exceptions.ValidationsException;

@Service
public class PasswordValidationService {

    public void validatePassword(String password) {
        validatePasswordPresence(password);
        Validator validator = Validator.link(
                new StringLengthValidator(
                        MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH, MIN_PASSWORD_LENGTH_MSG, MAX_PASSWORD_LENGTH_MSG),
                new StringContainsUpperCaseValidator(PASSWORD_UPPERCASE_LETTER_IS_REQUIRED_MSG),
                new StringContainsLowerCaseValidator(PASSWORD_LOWERCASE_LETTER_IS_REQUIRED_MSG),
                new StringContainsDigitValidator(PASSWORD_DIGIT_IS_REQUIRED_MSG),
                new StringContainsSpecialCharactersValidator(PASSWORD_SPECIAL_CHAR_IS_REQUIRED_MSG));
        List<String> errors = validator.validate(password);
        if (!errors.isEmpty()) {
            throw new ValidationsException(errors);
        }
    }

    public void comparePasswords(String password, String confirmedPassword) {
        if (!password.equals(confirmedPassword)) {
            throw new ValidationException(PASSWORDS_DO_NOT_MATCH_MSG);
        }
    }

    private void validatePasswordPresence(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException(PASSWORD_IS_REQUIRED_MSG);
        }
    }
}
