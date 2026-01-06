package pl.dgrecki.validators;

import static pl.dgrecki.constants.ValidationErrorMessages.*;

import java.util.List;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.ValidationsException;

@Service
public class UserDataValidationService {
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 100;

    public void validateFirstName(String firstName) {
        validateUserDataPresence(firstName, FIRST_NAME_IS_REQUIRED_MSG);
        Validator validator = Validator.link(new StringLengthValidator(
                MIN_LENGTH, MAX_LENGTH, MIN_FIRST_NAME_LENGTH_MSG, MAX_FIRST_NAME_LENGTH_MSG));
        List<String> errors = validator.validate(firstName);
        if (!errors.isEmpty()) {
            throw new ValidationsException(errors);
        }
    }

    public void validateLastName(String lastName) {
        validateUserDataPresence(lastName, LAST_NAME_IS_REQUIRED_MSG);
        Validator validator = Validator.link(
                new StringLengthValidator(MIN_LENGTH, MAX_LENGTH, MIN_LAST_NAME_LENGTH_MSG, MAX_LAST_NAME_LENGTH_MSG));
        List<String> errors = validator.validate(lastName);
        if (!errors.isEmpty()) {
            throw new ValidationsException(errors);
        }
    }

    private void validateUserDataPresence(String data, String exceptionMessage) {
        if (data == null || data.isEmpty()) {
            throw new ValidationsException(List.of(exceptionMessage));
        }
    }
}
