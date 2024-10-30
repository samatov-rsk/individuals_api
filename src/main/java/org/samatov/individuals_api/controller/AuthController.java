package org.samatov.individuals_api.controller;

import lombok.RequiredArgsConstructor;
import org.samatov.individuals_api.dto.CreateUserRequest;
import org.samatov.individuals_api.dto.LoginRequest;
import org.samatov.individuals_api.dto.TokenResponse;
import org.samatov.individuals_api.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TokenResponse> registerUser(@RequestBody CreateUserRequest request) {
        return authService.registerUser(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TokenResponse> loginUser(@RequestBody LoginRequest request) {
        return authService.loginUser(request);
    }

    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TokenResponse> refreshToken(@RequestBody String refsherTokenRequest) {
        return authService.refreshToken(refsherTokenRequest);
    }
}
