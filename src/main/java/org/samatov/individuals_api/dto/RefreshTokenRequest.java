package org.samatov.individuals_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @Schema(description = "ID клиента приложения", example = "individuals-api", required = true)
        String client_id,

        @Schema(description = "Секрет клиента приложения", example = "секретный_ключ", required = true)
        String client_secret,

        @Schema(description = "Refresh токен для обновления доступа", required = true)
        @NotBlank(message = "Refresh токен обязателен")
        String refresh_token,

        @Schema(description = "Тип доступа, должен быть 'refresh_token'", example = "refresh_token", required = true)
        String grant_type
) {}
