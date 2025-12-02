package org.example.validators;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

@AllArgsConstructor
public class StringContainsSpecialCharactersValidator extends Validator {
    private static final Pattern SPECIAL_CHARACTER_PATTERN =
            Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    private final String message;

    @Override
    protected void check(String text, List<String> errors) {
        if (!SPECIAL_CHARACTER_PATTERN.matcher(text).matches()) {
            errors.add(message);
        }
    }
}
