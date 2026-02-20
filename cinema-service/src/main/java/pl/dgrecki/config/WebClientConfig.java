package pl.dgrecki.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${payment-provider.sandbox.host}")
    private String sandboxPaymentProviderHost;

    @Bean
    public WebClient sandboxPaymentProviderWebClient() {
        return WebClient.builder().baseUrl(sandboxPaymentProviderHost).build();
    }
}
