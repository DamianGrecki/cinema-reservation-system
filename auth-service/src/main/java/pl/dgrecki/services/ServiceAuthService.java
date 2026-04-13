package pl.dgrecki.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.dgrecki.models.entities.ServiceCredential;
import pl.dgrecki.models.responses.JwtTokenResponse;
import pl.dgrecki.repositories.ServiceCredentialRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceAuthService {

    private final ServiceCredentialRepository serviceCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public JwtTokenResponse authenticate(String name, String clientSecret) {
        ServiceCredential service = serviceCredentialRepository
                .findByName(name)
                .filter(ServiceCredential::isActive)
                .orElseThrow(() -> new BadCredentialsException("Invalid service credentials"));

        if (!passwordEncoder.matches(clientSecret, service.getClientSecret())) {
            throw new BadCredentialsException("Invalid service credentials");
        }

        log.info("Service '{}' authenticated successfully", service.getName());
        return jwtService.generateServiceToken(service);
    }
}
