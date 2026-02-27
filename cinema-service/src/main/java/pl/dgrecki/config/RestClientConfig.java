package pl.dgrecki.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${payment-provider.sandbox.host}")
    private String sandboxPaymentProviderHost;

    @Bean
    public RestClient sandboxPaymentProviderRestClient() {
        return RestClient.builder().baseUrl(sandboxPaymentProviderHost).build();
    }
}
