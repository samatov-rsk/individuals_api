package org.samatov.individuals_api.controller;

import lombok.RequiredArgsConstructor;
import org.samatov.individuals_api.dto.UserResponse;
import org.samatov.individuals_api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<UserResponse> getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }
}
