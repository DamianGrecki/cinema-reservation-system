package org.example.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityFilterChainIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @ParameterizedTest
    @WithMockUser
    @MethodSource("nonExistingEndpoints")
    void shouldReturnStatusNotFoundWhenEndpointDoesNotExistAndUserIsAuthorizedTest(String endpoint) {
        mockMvc.perform(get(endpoint)).andExpect(status().isNotFound());
    }

    private static Stream<Arguments> nonExistingEndpoints() {
        return Stream.of(Arguments.of("/api/test1111"), Arguments.of("/test1111"));
    }
}
