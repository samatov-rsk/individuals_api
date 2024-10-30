package org.samatov.individuals_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient keycloakWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081/auth") // Замените на актуальный URL Keycloak сервера
                .build();
    }
}