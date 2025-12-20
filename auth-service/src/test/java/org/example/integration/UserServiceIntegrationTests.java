package org.example.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.example.exceptions.ResourceAlreadyExistsException;
import org.example.models.User;
import org.example.models.requests.UserRegisterRequest;
import org.example.models.responses.UserRegisterResponse;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
class UserServiceIntegrationTests extends BaseIntegrationTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Test
    void shouldRegisterCustomerUserTest() {
        String email = "test@example.com";
        String password = "Password123!";

        UserRegisterRequest request = new UserRegisterRequest(email, password, password);

        UserRegisterResponse response = userService.registerCustomer(request);
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

        UserRegisterRequest request = new UserRegisterRequest(email, password, password);

        userService.registerCustomer(request);
        assertEquals(1, userRepository.count());

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.registerCustomer(request));
        assertEquals(1, userRepository.count());
    }
}
