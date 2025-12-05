package org.example.validators;

import java.util.List;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StringContainsDigitValidator extends Validator {
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private final String message;

    @Override
    protected void check(String text, List<String> errors) {
        if (!DIGIT_PATTERN.matcher(text).matches()) {
            errors.add(message);
        }
    }
}
