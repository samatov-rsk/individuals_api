package org.samatov.individuals_api.dto;

public record LoginRequest(
        String email,
        String password) {
}
