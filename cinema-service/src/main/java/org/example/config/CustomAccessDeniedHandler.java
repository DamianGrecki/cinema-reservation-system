package org.example.config;

import static org.example.constants.ExceptionMessages.ACCESS_DENIED_MSG;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.models.responses.ErrorResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        log.info("Access denied exception occurred: ", accessDeniedException);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);

        ErrorResponse errorResponse = new ErrorResponse(ACCESS_DENIED_MSG);
        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}
