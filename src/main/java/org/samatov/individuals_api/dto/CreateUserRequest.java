package org.samatov.individuals_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @Schema(description = "First name of the user", example = "John", required = true)
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Last name of the user", example = "Doe", required = true)
        @NotBlank(message = "Last name is required")
        String lastname,

        @Schema(description = "Email address of the user", example = "john.doe@example.com", required = true)
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @Schema(description = "Password for the user account", example = "SecurePassword123", required = true)
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password should have at least 8 characters")
        String password,

        @Schema(description = "Password confirmation", example = "SecurePassword123", required = true)
        @NotBlank(message = "Password confirmation is required")
        String confirmPassword
) {
}
