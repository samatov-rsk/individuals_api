package org.samatov.individuals_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "DTO для создания пользователя в Keycloak")
public class KeycloakUserDto {

    @Schema(description = "Имя пользователя в Keycloak", example = "user123")
    private String username;

    @Schema(description = "Email пользователя", example = "user123@example.com")
    private String email;

    @Schema(description = "Имя пользователя", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String lastName;

    @Schema(description = "Статус пользователя: активен или нет", example = "true")
    private boolean enabled = true;

    @Schema(description = "Подтвержден ли email пользователя", example = "true")
    private boolean emailVerified = true;

    @Schema(description = "Учетные данные пользователя для входа в систему")
    private List<Map<String, Object>> credentials;

    public KeycloakUserDto(CreateUserRequest request) {
        this.username = request.email();
        this.email = request.email();
        this.firstName = request.firstName();
        this.lastName = request.lastname();
        this.enabled = true;
        this.emailVerified = true;
        this.credentials = Collections.singletonList(Map.of(
                "type", "password",
                "value", request.password(),
                "temporary", false
        ));
    }
}
