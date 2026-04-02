package pl.dgrecki.config;

import static pl.dgrecki.constants.Endpoints.*;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.dgrecki.services.user.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ServiceJwtFilter serviceJwtFilter;

    @Bean
    @SneakyThrows
    @Order(3)
    public SecurityFilterChain internalApiChain(HttpSecurity http) {
        return http.securityMatcher("/api/internal/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers(INTERNAL_TOKEN_ENDPOINT)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(serviceJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @SneakyThrows
    @Order(4)
    public SecurityFilterChain apiChain(HttpSecurity http) {
        http.securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                LOGIN_ENDPOINT,
                                REGISTER_CUSTOMER_ENDPOINT,
                                USER_ACTIVATE_ENDPOINT,
                                REFRESH_TOKEN_ENDPOINT,
                                LOGOUT_ENDPOINT)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .authenticationProvider(daoAuthenticationProvider());
        return http.build();
    }

    @Bean
    @Order(5)
    @SneakyThrows
    public SecurityFilterChain defaultChain(HttpSecurity http) {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @SneakyThrows
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
