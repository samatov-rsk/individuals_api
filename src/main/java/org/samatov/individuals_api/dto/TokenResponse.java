package org.samatov.individuals_api.dto;

public record TokenResponse(
        String access_token,
        String refresh_token,
        String token_type,
        int expires_in) {
}
