package org.samatov.individuals_api.client;

import org.samatov.individuals_api.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class KeycloakClient {
    private final WebClient webClient;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    public KeycloakClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<UserResponse> createUser(String email, String password, String confirmPassword) {
        return webClient.post()
                .uri(authServerUrl + "/admin/realms/" + realm + "/users")
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .bodyValue(new CreateUserRequest(email, password, confirmPassword))
                .retrieve()
                .bodyToMono(UserResponse.class);
    }

    public Mono<TokenResponse> getToken(String username, String password) {
        return webClient.post()
                .uri(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .bodyValue(new TokenRequest(clientId, clientSecret, username, password, "password"))
                .retrieve()
                .bodyToMono(TokenResponse.class);
    }

    public Mono<TokenResponse> refreshToken(String refreshToken) {
        return webClient.post()
                .uri(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .bodyValue(new RefreshTokenRequest(clientId, clientSecret, refreshToken, "refresh_token"))
                .retrieve()
                .bodyToMono(TokenResponse.class);
    }

    public Mono<UserResponse> getUserById(String userId) {
        return webClient.get()
                .uri(authServerUrl + "/admin/realms/" + realm + "/users/" + userId)
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .retrieve()
                .bodyToMono(UserResponse.class);
    }
}