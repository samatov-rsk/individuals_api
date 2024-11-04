package org.samatov.individuals_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Email пользователя для входа", example = "ivan.ivanov@example.com", required = true)
        @NotBlank(message = "Email обязателен")
        String username,

        @Schema(description = "Пароль пользователя для входа", example = "SecurePassword123", required = true)
        @NotBlank(message = "Пароль обязателен")
        String password
) {}
