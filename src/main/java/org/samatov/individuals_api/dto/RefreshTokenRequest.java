package org.samatov.individuals_api.dto;

public record RefreshTokenRequest(
        String client_id,
        String client_secret,
        String refresh_token,
        String grant_type) {
}
