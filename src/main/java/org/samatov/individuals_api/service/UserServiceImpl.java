package org.samatov.individuals_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.samatov.individuals_api.client.KeycloakClient;
import org.samatov.individuals_api.dto.UserResponse;
import org.samatov.individuals_api.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KeycloakClient keycloakClient;

    public Mono<UserResponse> getUserById(String userId) {
        return keycloakClient.getUserById(userId)
                .doOnSuccess(user -> log.info("Fetched user data for user ID: {}", userId))
                .onErrorResume(e -> {
                    log.error("Failed to fetch user data for user ID: {}", userId, e);
                    return Mono.error(new UserNotFoundException("User not found", e));
                });
    }
}