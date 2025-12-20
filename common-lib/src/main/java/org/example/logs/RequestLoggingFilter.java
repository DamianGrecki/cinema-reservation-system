package org.example.logs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Set<String> SENSITIVE_FIELDS =
            Set.of("password", "confirmPassword", "jwtToken", "secret", "apiKey");

    private static final String MASKED_VALUE = "*****";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();
        filterChain.doFilter(wrappedRequest, wrappedResponse);
        long time = System.currentTimeMillis() - start;

        logRequestEndpoint(request);

        String wrappedRequestBody = getWrappedRequestBodyString(wrappedRequest);
        logRequestBody(wrappedRequestBody);

        String wrappedResponseBody = getWrappedResponseBodyString(wrappedResponse);
        logResponse(request, response, wrappedResponseBody, time);
        wrappedResponse.copyBodyToResponse();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    private void logRequestEndpoint(HttpServletRequest request) {
        log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
    }

    @SneakyThrows
    private String getWrappedRequestBodyString(ContentCachingRequestWrapper wrappedRequest) {
        return new String(wrappedRequest.getContentAsByteArray(), wrappedRequest.getCharacterEncoding());
    }

    @SneakyThrows
    private String getWrappedResponseBodyString(ContentCachingResponseWrapper wrappedResponse) {
        return new String(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding());
    }

    private void logRequestBody(String requestBody) {
        if (log.isDebugEnabled()) {
            String maskedBody = maskSensitiveValues(requestBody);
            log.debug("Request body: {}", maskedBody);
        }
    }

    private void logResponse(HttpServletRequest request, HttpServletResponse response, String responseBody, long time) {
        log.info(
                "Response: {} {} -> status={} ({} ms)",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                time);
        if (log.isDebugEnabled()) {
            String maskedBody = maskSensitiveValues(responseBody);
            log.debug("Response body: {}", maskedBody);
        }
    }

    @SneakyThrows
    private String maskSensitiveValues(String body) {
        if (body == null || body.isBlank()) {
            return body;
        }
        JsonNode rootNode = objectMapper.readTree(body);
        maskNode(rootNode);
        return objectMapper.writeValueAsString(rootNode);
    }

    private void maskNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode childNode = objectNode.get(fieldName);
                if (SENSITIVE_FIELDS.contains(fieldName)) {
                    objectNode.put(fieldName, MASKED_VALUE);
                } else {
                    maskNode(childNode);
                }
            });
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                maskNode(arrayItem);
            }
        }
    }
}
