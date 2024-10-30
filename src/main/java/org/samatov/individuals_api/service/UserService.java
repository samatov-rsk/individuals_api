package org.samatov.individuals_api.service;

import org.samatov.individuals_api.dto.UserResponse;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<UserResponse> getUserById(String userId);
}
