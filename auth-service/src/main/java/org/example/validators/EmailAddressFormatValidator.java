package org.example.validators;

import java.util.List;
import java.util.regex.Pattern;

import static org.example.constants.Messages.EMAIL_ADDRESS_FORMAT_IS_INVALID_MSG;

public class EmailAddressFormatValidator extends Validator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    protected void check(String email, List<String> errors) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            errors.add(EMAIL_ADDRESS_FORMAT_IS_INVALID_MSG);
        }
    }
}
