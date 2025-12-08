package org.example.unit;

import static org.example.constants.PasswordConstraints.MAX_PASSWORD_LENGTH;
import static org.example.constants.PasswordConstraints.MIN_PASSWORD_LENGTH;
import static org.example.constants.ValidationErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.example.exceptions.ValidationException;
import org.example.exceptions.ValidationsException;
import org.example.validators.PasswordValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
class PasswordValidationServiceUnitTests {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PasswordValidationService passwordValidationService = new PasswordValidationService();

    @ParameterizedTest
    @MethodSource("invalidPasswordProvider")
    void validatePasswordShouldThrowForInvalidPasswordTest(String password, String expectedMessage) {

        ValidationsException ex =
                assertThrows(ValidationsException.class, () -> passwordValidationService.validatePassword(password));
        List<String> errors = ex.getMessages();
        assertEquals(1, errors.size());
        assertTrue(ex.getMessages().contains(expectedMessage));
    }

    private static Stream<Arguments> invalidPasswordProvider() {
        return Stream.of(
                Arguments.of("password123!", PASSWORD_UPPERCASE_LETTER_IS_REQUIRED_MSG),
                Arguments.of("PASSWORD123!", PASSWORD_LOWERCASE_LETTER_IS_REQUIRED_MSG),
                Arguments.of("Password123", PASSWORD_SPECIAL_CHAR_IS_REQUIRED_MSG),
                Arguments.of("Password!", PASSWORD_DIGIT_IS_REQUIRED_MSG));
    }

    @ParameterizedTest
    @MethodSource("lengthPasswordProvider")
    void validatePasswordShouldThrowForInvalidLengthTest(String password, String expectedMessage) {

        ValidationsException ex =
                assertThrows(ValidationsException.class, () -> passwordValidationService.validatePassword(password));

        assertEquals(expectedMessage, ex.getMessages().getFirst());
    }

    private static Stream<Arguments> lengthPasswordProvider() {
        return Stream.of(
                Arguments.of("P1!", String.format(MIN_PASSWORD_LENGTH_MSG, MIN_PASSWORD_LENGTH)),
                Arguments.of(
                        "Password123!" + "!".repeat(MAX_PASSWORD_LENGTH),
                        String.format(MAX_PASSWORD_LENGTH_MSG, MAX_PASSWORD_LENGTH)));
    }

    @Test
    void validatePasswordShouldThrowForEmptyPasswordTest() {
        ValidationException exception =
                assertThrows(ValidationException.class, () -> passwordValidationService.validatePassword(""));
        assertEquals(PASSWORD_IS_REQUIRED_MSG, exception.getMessage());
    }

    @Test
    void validatePasswordShouldNotThrowForValidPasswordTest() {
        assertDoesNotThrow(() -> passwordValidationService.validatePassword("Password123!"));
    }

    @Test
    void comparePasswordsShouldThrowForNotEqualsPasswordsTest() {
        String password = "Password123!";
        String confirmedPassword = "Password123!!";
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> passwordValidationService.comparePasswords(password, confirmedPassword));
        assertEquals(PASSWORDS_DO_NOT_MATCH_MSG, exception.getMessage());
    }

    @Test
    void comparePasswordsShouldNotThrowForEqualsPasswordsTest() {
        String password = "Password123!";
        assertDoesNotThrow(() -> passwordValidationService.comparePasswords(password, password));
    }

    @Test
    void encodePasswordTest() {
        String password = "Password123!";
        String encodedPassword = passwordEncoder.encode(password);
        assertTrue(passwordEncoder.matches(password, encodedPassword));
    }
}
