package org.example.validators;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

@AllArgsConstructor
public class StringContainsLowerCaseValidator extends Validator {
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private final String message;

    @Override
    protected void check(String text, List<String> errors) {
        if (!UPPERCASE_PATTERN.matcher(text).matches()) {
            errors.add(message);
        }
    }
}
