package org.example.unit;

import static org.example.constants.EmailAddressConstraints.MAX_EMAIL_ADDRESS_LENGTH;
import static org.example.constants.EmailAddressConstraints.MIN_EMAIL_ADDRESS_LENGTH;
import static org.example.constants.Messages.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.example.exceptions.ValidationsException;
import org.example.validators.EmailAddressValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@RequiredArgsConstructor
class EmailAddressValidationServiceUnitTests {
    private final EmailAddressValidationService emailValidationService = new EmailAddressValidationService();

    @ParameterizedTest
    @MethodSource("invalidEmailProvider")
    void validateEmailShouldThrowForInvalidEmailTest(String email) {

        ValidationsException ex =
                assertThrows(ValidationsException.class, () -> emailValidationService.validateEmail(email));
        List<String> errors = ex.getMessages();
        assertEquals(1, errors.size());
        assertEquals(EMAIL_ADDRESS_FORMAT_IS_INVALID_MSG, ex.getMessages().getFirst());
    }

    private static Stream<Arguments> invalidEmailProvider() {
        return Stream.of(
                Arguments.of("userexample.com"),
                Arguments.of("user@"),
                Arguments.of("@example.com"),
                Arguments.of("user@example.c"),
                Arguments.of("user!@example.com"));
    }

    @ParameterizedTest
    @MethodSource("lengthEmailProvider")
    void validateEmailShouldThrowForInvalidLengthTest(String email, String expectedMessage) {

        ValidationsException ex =
                assertThrows(ValidationsException.class, () -> emailValidationService.validateEmail(email));

        assertEquals(expectedMessage, ex.getMessages().getFirst());
    }

    private static Stream<Arguments> lengthEmailProvider() {
        return Stream.of(
                Arguments.of("a@pl", String.format(MIN_EMAIL_ADDRESS_LENGTH_MSG, MIN_EMAIL_ADDRESS_LENGTH)),
                Arguments.of(
                        "test@exampl" + "e".repeat(MAX_EMAIL_ADDRESS_LENGTH) + ".com",
                        String.format(MAX_EMAIL_ADDRESS_LENGTH_MSG, MAX_EMAIL_ADDRESS_LENGTH)));
    }

    @Test
    void validateEmailShouldThrowForEmptyEmailTest() {
        ValidationsException exception =
                assertThrows(ValidationsException.class, () -> emailValidationService.validateEmail(""));
        List<String> exceptions = exception.getMessages();
        assertEquals(1, exceptions.size());
        assertEquals(EMAIL_ADDRESS_IS_REQUIRED_MSG, exceptions.getFirst());
    }

    @Test
    void validateEmailShouldNotThrowForValidEmailTest() {
        assertDoesNotThrow(() -> emailValidationService.validateEmail("test@example.com"));
    }
}
