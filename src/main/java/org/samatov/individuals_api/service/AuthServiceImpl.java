package org.samatov.individuals_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.samatov.individuals_api.client.KeycloakClient;
import org.samatov.individuals_api.dto.CreateUserRequest;
import org.samatov.individuals_api.dto.LoginRequest;
import org.samatov.individuals_api.dto.TokenResponse;
import org.samatov.individuals_api.exception.AuthenticationException;
import org.samatov.individuals_api.exception.InvalidPasswordException;
import org.samatov.individuals_api.exception.RegistrationException;
import org.samatov.individuals_api.exception.TokenRefreshException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakClient keycloakClient;

    public Mono<TokenResponse> registerUser(CreateUserRequest createUserRequest) {
        if (!createUserRequest.password().equals(createUserRequest.confirmPassword())) {
            log.warn("Password confirmation does not match for email: {}", createUserRequest.email());
            return Mono.error(new InvalidPasswordException("Password confirmation does not match"));
        }

        return keycloakClient.createUser(createUserRequest.email(), createUserRequest.password(), createUserRequest.confirmPassword())
                .doOnSuccess(user -> log.info("User registered successfully with email: {}", createUserRequest.email()))
                .flatMap(user -> keycloakClient.getToken(createUserRequest.email(), createUserRequest.password()))
                .doOnSuccess(token -> log.info("Token generated for user: {}", createUserRequest.email()))
                .onErrorResume(e -> {
                    log.error("Failed to register or generate token for user: {}", createUserRequest.email(), e);
                    return Mono.error(new RegistrationException("Registration failed", e));
                });
    }

    public Mono<TokenResponse> loginUser(LoginRequest loginRequest) {
        return keycloakClient.getToken(loginRequest.email(), loginRequest.password())
                .doOnSuccess(token -> log.info("User logged in successfully with email: {}", loginRequest.email()))
                .onErrorResume(e -> {
                    log.error("Login failed for email: {}", loginRequest.email(), e);
                    return Mono.error(new AuthenticationException("Invalid email or password", e));
                });
    }

    public Mono<TokenResponse> refreshToken(String refreshToken) {
        return keycloakClient.refreshToken(refreshToken)
                .doOnSuccess(token -> log.info("Token refreshed successfully"))
                .onErrorResume(e -> {
                    log.error("Token refresh failed", e);
                    return Mono.error(new TokenRefreshException("Refresh token is invalid or expired", e));
                });
    }
}

