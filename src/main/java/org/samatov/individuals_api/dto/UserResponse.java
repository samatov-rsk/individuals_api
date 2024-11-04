package org.samatov.individuals_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UserResponse(
        @Schema(description = "ID пользователя в системе", example = "c2d5abfb-a2e6-4530-8827-dc72504fdc68")
        String id,

        @Schema(description = "Имя пользователя", example = "ivan.ivanov@example.com")
        String username,

        @Schema(description = "Email пользователя", example = "ivan.ivanov@example.com")
        String email,

        @Schema(description = "Роли пользователя", example = "[\"USER\", \"ADMIN\"]")
        List<String> roles,

        @Schema(description = "Дата создания пользователя", example = "2024-11-04T16:36:11Z")
        @JsonProperty("created_at")
        String createdAt
) {}
