package org.example.unit;

import static org.example.constants.ExceptionMessages.ACCESS_DENIED_MSG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import lombok.SneakyThrows;
import org.example.config.CustomAccessDeniedHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

class CustomAccessDeniedHandlerUnitTests {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @SneakyThrows
    @Test
    void handleSendCorrectResponseTest() {
        CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        handler.handle(request, response, new AccessDeniedException("Test exception"));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");

        printWriter.flush();

        String json = stringWriter.toString();
        String message = JsonPath.read(json, "$.message");
        assertEquals(ACCESS_DENIED_MSG, message);
    }
}
