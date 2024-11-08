package org.samatov.individuals_api.service;

import org.samatov.individuals_api.dto.CreateUserRequest;
import org.samatov.individuals_api.dto.LoginRequest;
import org.samatov.individuals_api.dto.TokenResponse;
import org.samatov.individuals_api.dto.UserResponse;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<TokenResponse> registerUser(CreateUserRequest request);

    Mono<TokenResponse> loginUser(LoginRequest request);

    Mono<TokenResponse> refreshToken(String refreshToken);

    Mono<UserResponse> getUserInfo(String id , String accessToken);

}
