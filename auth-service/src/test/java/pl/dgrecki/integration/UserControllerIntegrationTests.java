package pl.dgrecki.integration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.dgrecki.constants.Endpoints.LOGIN_ENDPOINT;
import static pl.dgrecki.constants.Endpoints.REGISTER_CUSTOMER_ENDPOINT;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.enums.UserStatus;
import pl.dgrecki.models.requests.LoginRequest;
import pl.dgrecki.models.requests.UserRegisterRequest;
import pl.dgrecki.repositories.UserRepository;
import pl.dgrecki.services.user.UserRegistrationService;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    UserRegistrationService userRegistrationService;

    @Autowired
    UserRepository userRepository;

    @SneakyThrows
    @Test
    void shouldReturnJwtTokenWhenUserPassAuthenticationTest() {
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        registerAndActivateUser(email, password, firstName, lastName);
        LoginRequest request = new LoginRequest(email, password);

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken", notNullValue()));
    }

    @SneakyThrows
    @Test
    void shouldReturn400WhenUserIsNotActivatedTest() {
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        registerUser(email, password, firstName, lastName);
        LoginRequest request = new LoginRequest(email, password);

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("User account is not activated")));
    }

    @SneakyThrows
    @Test
    void shouldReturn401WhenUserHaveIncorrectCredentialsTest() {
        String email = "test@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        registerUser(email, password, firstName, lastName);
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
        String firstName = "John";
        String lastName = "Doe";
        UserRegisterRequest request = new UserRegisterRequest(email, password, password, firstName, lastName);

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

    private void registerUser(String email, String password, String firstName, String lastName) {
        UserRegisterRequest request = new UserRegisterRequest(email, password, password, firstName, lastName);
        userRegistrationService.registerCustomer(request);
    }

    private void registerAndActivateUser(String email, String password, String firstName, String lastName) {
        registerUser(email, password, firstName, lastName);
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
}
