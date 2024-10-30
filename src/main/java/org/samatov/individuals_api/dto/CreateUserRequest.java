package org.samatov.individuals_api.dto;

public record CreateUserRequest(
        String email,
        String password,
        String confirmPassword) {
}
