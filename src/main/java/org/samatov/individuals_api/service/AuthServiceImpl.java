package org.samatov.individuals_api.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.samatov.individuals_api.dto.*;
import org.samatov.individuals_api.exception.AuthenticationException;
import org.samatov.individuals_api.exception.UserAlreadyExistsException;
import org.samatov.individuals_api.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final WebClient webClient;

    @Value("${keycloak.auth-server-url}")
    private String KEYCLOAK_URL;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String CLIENT_SECRET;
    @Value("${keycloak.admin-password}")
    private String ADMIN_USERNAME;
    @Value("${keycloak.admin-password}")
    private String ADMIN_PASSWORD;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String CLIENT_ID;

    @Autowired
    public AuthServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }


    public Mono<TokenResponse> registerUser(CreateUserRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            log.error("Password confirmation does not match");
            return Mono.error(new IllegalArgumentException("Password confirmation does not match"));
        }

        return getAdminToken()
                .doOnNext(token -> log.info("Admin token retrieved successfully"))
                .flatMap(adminToken -> createUserInKeycloak(adminToken, request)
                        .onErrorResume(e -> {
                            if (e.getMessage().contains("User with this email already exists")) {
                                return Mono.error(new UserAlreadyExistsException("User with this email already exists"));
                            }
                            log.error("Error creating user in Keycloak: {}", e.getMessage());
                            return Mono.error(e);
                        })
                )
                .flatMap(userId -> loginUser(new LoginRequest(request.email(), request.password())))
                .doOnNext(token -> log.info("User login successful"))
                .doOnError(e -> log.error("Error during registration: {}", e.getMessage()));
    }

    public Mono<TokenResponse> loginUser(LoginRequest loginRequest) {
        log.info("Attempting login for email: {}", loginRequest.username());
        return webClient.post()
                .uri(KEYCLOAK_URL + "/realms/individuals/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", CLIENT_ID)
                        .with("client_secret", CLIENT_SECRET)
                        .with("grant_type", "password")
                        .with("username", loginRequest.username())
                        .with("password", loginRequest.password())
                        .with("scope", "openid profile email"))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    log.error("Login failed with status: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new AuthenticationException("Failed to login user: " + errorBody)));
                })
                .bodyToMono(TokenResponse.class);
    }

    public Mono<TokenResponse> refreshToken(String refreshToken) {
        log.info("Attempting to refresh token with refreshToken: {}", refreshToken);
        return webClient.post()
                .uri(KEYCLOAK_URL + "/realms/individuals/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", CLIENT_ID)
                        .with("client_secret", CLIENT_SECRET)
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnNext(token -> log.info("Token refreshed successfully"))
                .doOnError(e -> log.error("Error during token refresh: {}", e.getMessage()));
    }

    public Mono<UserResponse> getUserInfo(String userId, String accessToken) {
        return getAdminToken()
                .flatMap(adminToken -> webClient.get()
                        .uri(KEYCLOAK_URL + "/admin/realms/individuals/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(userDetails -> {
                            String username = (String) userDetails.get("username");
                            String email = (String) userDetails.get("email");
                            Long createdTimestamp = (Long) userDetails.get("createdTimestamp");

                            // Извлекаем роли из токена
                            List<String> roles = extractRolesFromToken(accessToken);

                            String createdAt = createdTimestamp != null
                                    ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .format(new java.util.Date(createdTimestamp))
                                    : null;

                            return new UserResponse(
                                    userId,
                                    username,
                                    email,
                                    roles,
                                    createdAt
                            );
                        })
                        .doOnNext(userInfo -> log.info("User info retrieved: {}", userInfo))
                        .doOnError(e -> log.error("Error retrieving user info: {}", e.getMessage())))
                .onErrorResume(e -> Mono.error(new UserNotFoundException("User not found")));
    }

    private List<String> extractRolesFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        List<String> roles = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access").asMap();
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            roles.addAll((List<String>) realmAccess.get("roles"));
        }

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access").asMap();
        if (resourceAccess != null) {
            for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                Map<String, Object> clientRoles = (Map<String, Object>) entry.getValue();
                if (clientRoles.containsKey("roles")) {
                    roles.addAll((List<String>) clientRoles.get("roles"));
                }
            }
        }
        return roles;
    }

    private Mono<String> getAdminToken() {
        return webClient.post()
                .uri(KEYCLOAK_URL + "/realms/master/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "admin-cli")
                        .with("username", ADMIN_USERNAME)
                        .with("password", ADMIN_PASSWORD))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::accessToken)
                .doOnNext(token -> log.info("Admin token received"))
                .doOnError(e -> log.error("Failed to retrieve admin token: {}", e.getMessage()));
    }

    private Mono<String> createUserInKeycloak(String adminToken, CreateUserRequest request) {
        KeycloakUserDto keycloakUserDto = new KeycloakUserDto(request);

        return webClient.post()
                .uri(KEYCLOAK_URL + "/admin/realms/individuals/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(keycloakUserDto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    log.error("User creation failed with status: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new UserAlreadyExistsException("User already exists: " + errorBody)));
                })
                .bodyToMono(Void.class)
                .then(Mono.just("User created successfully"));
    }
}
