package pl.dgrecki.config;

import static pl.dgrecki.constants.ExceptionMessages.INVALID_OR_EXPIRED_TOKEN_MSG;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.responses.ErrorResponse;

@Slf4j
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        log.info("Auth exception occurred: ", authException);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        ErrorResponse errorResponse = new ErrorResponse(INVALID_OR_EXPIRED_TOKEN_MSG);
        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}
