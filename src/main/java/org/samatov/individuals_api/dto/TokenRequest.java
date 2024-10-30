package org.samatov.individuals_api.dto;

public record TokenRequest(
        String client_id,
        String client_secret,
        String username,
        String password,
        String grant_type) {
}
