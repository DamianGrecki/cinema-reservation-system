package pl.dgrecki.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import pl.dgrecki.exceptions.ResourceAlreadyExistsException;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.requests.UserRegisterRequest;
import pl.dgrecki.models.responses.UserRegisterResponse;
import pl.dgrecki.repositories.UserRepository;
import pl.dgrecki.services.user.UserRegistrationService;

@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationServiceIntegrationTests extends BaseIntegrationTest {

    @Autowired
    UserRegistrationService userRegistrationService;

    @Autowired
    UserRepository userRepository;

    @Test
    void shouldRegisterCustomerUserTest() {
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";

        UserRegisterRequest request = new UserRegisterRequest(email, password, password, firstName, lastName);

        UserRegisterResponse response = userRegistrationService.registerCustomer(request);
        assertTrue(response.isSuccess());
        assertEquals(email, response.getEmail());

        assertEquals(1, userRepository.count());
        User savedUser = userRepository.findByEmail(email).orElseThrow();
        assertEquals(email, savedUser.getEmail());
        assertTrue(savedUser.getPassword().startsWith("$2"));
    }

    @Test
    void shouldThrowExceptionWhenEmailExistsTest() {
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";

        UserRegisterRequest request = new UserRegisterRequest(email, password, password, firstName, lastName);

        userRegistrationService.registerCustomer(request);
        assertEquals(1, userRepository.count());

        assertThrows(ResourceAlreadyExistsException.class, () -> userRegistrationService.registerCustomer(request));
        assertEquals(1, userRepository.count());
    }
}
