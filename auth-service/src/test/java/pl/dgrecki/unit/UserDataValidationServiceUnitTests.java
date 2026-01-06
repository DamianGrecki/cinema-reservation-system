package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static pl.dgrecki.constants.ValidationErrorMessages.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.dgrecki.exceptions.ValidationsException;
import pl.dgrecki.validators.UserDataValidationService;

class UserDataValidationServiceUnitTests {

    private UserDataValidationService service;
    private static final int MAX_LENGTH = 100;

    @BeforeEach
    void setUp() {
        service = new UserDataValidationService();
    }

    @Test
    void shouldValidateFirstNameSuccessfullyTest() {
        assertDoesNotThrow(() -> service.validateFirstName("John"));
    }

    @Test
    void shouldThrowWhenFirstNameIsNullTest() {
        ValidationsException ex = assertThrows(ValidationsException.class, () -> service.validateFirstName(null));
        assertEquals(1, ex.getMessages().size());
        assertEquals(FIRST_NAME_IS_REQUIRED_MSG, ex.getMessages().getFirst());
    }

    @Test
    void shouldThrowWhenFirstNameIsEmptyTest() {
        ValidationsException ex = assertThrows(ValidationsException.class, () -> service.validateFirstName(""));
        assertEquals(1, ex.getMessages().size());
        assertEquals(FIRST_NAME_IS_REQUIRED_MSG, ex.getMessages().getFirst());
    }

    @Test
    void shouldThrowWhenFirstNameTooLong() {
        String longName = "A".repeat(101);
        ValidationsException ex = assertThrows(ValidationsException.class, () -> service.validateFirstName(longName));
        assertEquals(1, ex.getMessages().size());
        assertEquals(
                String.format(MAX_FIRST_NAME_LENGTH_MSG, MAX_LENGTH),
                ex.getMessages().getFirst());
    }

    @Test
    void shouldValidateLastNameSuccessfullyTest() {
        assertDoesNotThrow(() -> service.validateLastName("Doe"));
    }

    @Test
    void shouldThrowWhenLastNameIsNullTest() {
        ValidationsException ex = assertThrows(ValidationsException.class, () -> service.validateLastName(null));
        assertEquals(1, ex.getMessages().size());
        assertEquals(LAST_NAME_IS_REQUIRED_MSG, ex.getMessages().getFirst());
    }

    @Test
    void shouldThrowWhenLastNameIsEmptyTest() {
        ValidationsException ex = assertThrows(ValidationsException.class, () -> service.validateLastName(""));
        assertEquals(1, ex.getMessages().size());
        assertEquals(LAST_NAME_IS_REQUIRED_MSG, ex.getMessages().getFirst());
    }

    @Test
    void shouldThrowWhenLastNameTooLong() {
        String longLastName = "A".repeat(101);
        ValidationsException ex =
                assertThrows(ValidationsException.class, () -> service.validateLastName(longLastName));
        assertEquals(1, ex.getMessages().size());
        assertEquals(
                String.format(MAX_LAST_NAME_LENGTH_MSG, MAX_LENGTH),
                ex.getMessages().getFirst());
    }
}
