package org.samatov.individuals_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "Access token для авторизации", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
        @JsonProperty("access_token")
        String accessToken,

        @Schema(description = "Время жизни access token в секундах", example = "3600")
        @JsonProperty("expires_in")
        int expires_in,

        @Schema(description = "Refresh token для обновления access token", example = "dGhpc0lzQXJlUmVmcmVzaFRva2Vu...")
        @JsonProperty("refresh_token")
        String refresh_token,

        @Schema(description = "Тип токена, обычно 'Bearer'", example = "Bearer")
        @JsonProperty("token_type")
        String token_type,
        String message
) {}
