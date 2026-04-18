package pl.dgrecki.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class CommonSecurityConfig {

    private final PasswordEncoder passwordEncoder;

    @Value("${prometheus.username:prometheus}")
    private String prometheusUsername;

    @Value("${prometheus.password:123}")
    private String prometheusPassword;

    private static final String PROMETHEUS_ROLE = "PROMETHEUS";

    @Bean
    @SneakyThrows
    @Order(1)
    public SecurityFilterChain prometheusChain(HttpSecurity http) {
        return http.securityMatcher("/actuator/prometheus")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole(PROMETHEUS_ROLE))
                .authenticationManager(prometheusAuthenticationManager())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    @SneakyThrows
    @Order(2)
    public SecurityFilterChain swaggerChain(HttpSecurity http) {
        http.securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public AuthenticationManager prometheusAuthenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(prometheusUserDetailsService());
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public UserDetailsService prometheusUserDetailsService() {
        UserDetails prometheus = User.builder()
                .username(prometheusUsername)
                .password(passwordEncoder.encode(prometheusPassword))
                .roles(PROMETHEUS_ROLE)
                .build();
        return new InMemoryUserDetailsManager(prometheus);
    }
}
