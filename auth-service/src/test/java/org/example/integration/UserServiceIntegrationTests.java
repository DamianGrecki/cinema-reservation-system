package org.example.integration;

import org.example.exceptions.ResourceAlreadyExistsException;
import org.example.models.User;
import org.example.models.requests.UserRegisterRequest;
import org.example.models.responses.UserRegisterResponse;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceIntegrationTests extends BaseIntegrationTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Test
    void shouldRegisterUserTest() {
        String email = "test@example.com";
        String password = "Password123!";

        UserRegisterRequest request = new UserRegisterRequest(
                email,
                password,
                password
        );

        UserRegisterResponse response = userService.register(request);
        assertTrue(response.isSuccess());
        assertEquals(email, response.getEmail());

        assertEquals(1, userRepository.count());
        User savedUser = userRepository.findByEmail(email)
                .orElseThrow();
        assertEquals(email, savedUser.getEmail());
        assertTrue(savedUser.getPassword().startsWith("$2"));
    }

    @Test
    void shouldThrowExceptionWhenEmailExistsTest() {
        String email = "test@example.com";
        String password = "Password123!";

        UserRegisterRequest request = new UserRegisterRequest(
                email,
                password,
                password
        );

        userService.register(request);
        assertEquals(1, userRepository.count());

        assertThrows(ResourceAlreadyExistsException.class,
                () -> userService.register(request)
        );
        assertEquals(1, userRepository.count());
    }
}
