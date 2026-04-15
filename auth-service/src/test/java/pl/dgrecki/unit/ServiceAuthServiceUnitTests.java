package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dgrecki.models.entities.ServiceCredential;
import pl.dgrecki.models.entities.ServiceRole;
import pl.dgrecki.models.enums.ServiceRoleType;
import pl.dgrecki.models.responses.JwtTokenResponse;
import pl.dgrecki.repositories.ServiceCredentialRepository;
import pl.dgrecki.services.JwtService;
import pl.dgrecki.services.ServiceAuthService;

@ExtendWith(MockitoExtension.class)
class ServiceAuthServiceUnitTests {

    @Mock
    private ServiceCredentialRepository serviceCredentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ServiceAuthService serviceAuthService;

    @Test
    void shouldAuthenticateServiceWithValidCredentialsTest() {
        ServiceCredential service = createServiceCredential("cinema-service", "hashed-secret", true);
        JwtTokenResponse expectedResponse =
                new JwtTokenResponse("service-jwt-token", Instant.parse("2026-03-23T12:05:00Z"));

        when(serviceCredentialRepository.findByName("cinema-service")).thenReturn(Optional.of(service));
        when(passwordEncoder.matches("raw-secret", "hashed-secret")).thenReturn(true);
        when(jwtService.generateServiceToken(service)).thenReturn(expectedResponse);

        JwtTokenResponse response = serviceAuthService.authenticate("cinema-service", "raw-secret");

        assertEquals("service-jwt-token", response.getJwtToken());
        assertNotNull(response.getExpiresAt());
        verify(jwtService).generateServiceToken(service);
    }

    @Test
    void shouldThrowWhenServiceNotFoundTest() {
        when(serviceCredentialRepository.findByName("unknown-service")).thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class, () -> serviceAuthService.authenticate("unknown-service", "any-secret"));

        verify(jwtService, never()).generateServiceToken(any());
    }

    @Test
    void shouldThrowWhenServiceIsInactiveTest() {
        ServiceCredential service = createServiceCredential("cinema-service", "hashed-secret", false);

        when(serviceCredentialRepository.findByName("cinema-service")).thenReturn(Optional.of(service));

        assertThrows(
                BadCredentialsException.class, () -> serviceAuthService.authenticate("cinema-service", "raw-secret"));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateServiceToken(any());
    }

    @Test
    void shouldThrowWhenPasswordDoesNotMatchTest() {
        ServiceCredential service = createServiceCredential("cinema-service", "hashed-secret", true);

        when(serviceCredentialRepository.findByName("cinema-service")).thenReturn(Optional.of(service));
        when(passwordEncoder.matches("wrong-secret", "hashed-secret")).thenReturn(false);

        assertThrows(
                BadCredentialsException.class, () -> serviceAuthService.authenticate("cinema-service", "wrong-secret"));

        verify(jwtService, never()).generateServiceToken(any());
    }

    private ServiceCredential createServiceCredential(String name, String secret, boolean active) {
        ServiceRole role = new ServiceRole(ServiceRoleType.READ_USERS);
        ServiceCredential service = new ServiceCredential();
        ReflectionTestUtils.setField(service, "id", 1L);
        ReflectionTestUtils.setField(service, "name", name);
        ReflectionTestUtils.setField(service, "clientSecret", secret);
        ReflectionTestUtils.setField(service, "active", active);
        ReflectionTestUtils.setField(service, "roles", Set.of(role));
        return service;
    }
}
