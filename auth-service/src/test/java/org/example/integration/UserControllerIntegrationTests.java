package org.example.integration;

import static org.example.constants.Endpoints.LOGIN_ENDPOINT;
import static org.example.constants.Endpoints.REGISTER_CUSTOMER_ENDPOINT;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.example.models.User;
import org.example.models.requests.LoginRequest;
import org.example.models.requests.UserRegisterRequest;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @SneakyThrows
    @Test
    void shouldReturnJwtTokenWhenUserPassAuthenticationTest() {
        String email = "test@example.com";
        String password = "Password123!";
        registerUser(email, password);
        LoginRequest request = new LoginRequest(email, password);

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken", notNullValue()));
    }

    @SneakyThrows
    @Test
    void shouldReturn401WhenUserHaveIncorrectCredentialsTest() {
        String email = "test@example.com";
        String password = "Password123!";
        registerUser(email, password);
        LoginRequest request = new LoginRequest(email, "WrongPassword!");

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.jwtToken").doesNotExist());
    }

    @SneakyThrows
    @Test
    void shouldReturnSuccessTrueWhenUserPassRegistrationTest() {
        String email = "test@example.com";
        String password = "Password123!";
        UserRegisterRequest request = new UserRegisterRequest(email, password, password);

        mockMvc.perform(post(REGISTER_CUSTOMER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)));

        assertEquals(1, userRepository.count());
        User savedUser = userRepository.findByEmail(email).orElseThrow();
        assertEquals(email, savedUser.getEmail());
        assertTrue(savedUser.getPassword().startsWith("$2"));
    }

    private void registerUser(String email, String password) {
        UserRegisterRequest request = new UserRegisterRequest(email, password, password);
        userService.registerCustomer(request);
    }
}
