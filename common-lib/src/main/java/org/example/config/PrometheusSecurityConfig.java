package org.example.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
public class PrometheusSecurityConfig {

    private final PasswordEncoder passwordEncoder;

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
    public AuthenticationManager prometheusAuthenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(prometheusUserDetailsService());
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public UserDetailsService prometheusUserDetailsService() {
        UserDetails prometheus = User.builder()
                .username("prometheus") // It will eventually be moved to .env
                .password(passwordEncoder.encode("123")) // It will eventually be moved to .env
                .roles(PROMETHEUS_ROLE)
                .build();
        return new InMemoryUserDetailsManager(prometheus);
    }
}
