package org.samatov.individuals_api.dto;

public record UserResponse(
        String id,
        String username,
        String email) {
}
