package pl.dgrecki.config;

import static pl.dgrecki.constants.Endpoints.*;
import static pl.dgrecki.constants.Webhooks.PAYMENT_PROVIDER_WEBHOOK;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    @SneakyThrows
    @Order(3)
    public SecurityFilterChain apiChain(HttpSecurity http) {
        return http.securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(
                        ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint).accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                MOVIES_ENDPOINT,
                                SCREENINGS_ENDPOINT,
                                SCREENINGS_SEATS_ENDPOINT,
                                BASKET_ENDPOINT,
                                BASKET_PRICING_ENDPOINT,
                                RESERVATION_ENDPOINT,
                                RESERVATIONS_CANCEL_ENDPOINT,
                                RESERVATION_PRICING_TYPE_ENDPOINT,
                                TICKET_ENDPOINT,
                                ORDER_TICKETS_DOWNLOAD_ENDPOINT,
                                PAYMENT_ENDPOINT,
                                PAYMENT_PROVIDER_WEBHOOK)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(4)
    @SneakyThrows
    public SecurityFilterChain defaultChain(HttpSecurity http) {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
        return http.build();
    }
}
