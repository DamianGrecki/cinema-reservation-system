package org.example.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Messages {
    public static final String PASSWORDS_DO_NOT_MATCH_MSG = "Passwords do not match.";
    public static final String PASSWORD_IS_REQUIRED_MSG = "Password is required.";
    public static final String MIN_PASSWORD_LENGTH_MSG = "Password length should be more than %d chars.";
    public static final String MAX_PASSWORD_LENGTH_MSG = "Password length should be less than %d chars.";
    public static final String PASSWORD_UPPERCASE_LETTER_IS_REQUIRED_MSG = "Password should contain an uppercase letter.";
    public static final String PASSWORD_LOWERCASE_LETTER_IS_REQUIRED_MSG = "Password should contain an lowercase letter.";
    public static final String PASSWORD_DIGIT_IS_REQUIRED_MSG = "Password should contain a digit";
    public static final String PASSWORD_SPECIAL_CHAR_IS_REQUIRED_MSG = "Password should contain an special character.";

    public static final String MIN_EMAIL_ADDRESS_LENGTH_MSG = "Email address length should be more than %d chars.";
    public static final String MAX_EMAIL_ADDRESS_LENGTH_MSG = "Email address length should be less than %d chars.";
    public static final String EMAIL_ADDRESS_FORMAT_IS_INVALID_MSG = "Email format is invalid.";
    public static final String EMAIL_ADDRESS_ALREADY_EXISTS_MSG = "User with email '%s' already exists.";
    public static final String EMAIL_ADDRESS_IS_REQUIRED_MSG = "Email is required.";
}
