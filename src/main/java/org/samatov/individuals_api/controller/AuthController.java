package org.samatov.individuals_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.samatov.individuals_api.dto.*;
import org.samatov.individuals_api.exception.AuthenticationException;
import org.samatov.individuals_api.exception.UserAlreadyExistsException;
import org.samatov.individuals_api.exception.UserNotFoundException;
import org.samatov.individuals_api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;


@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Регистрация пользователя",
            description = "Регистрирует нового пользователя по email и паролю, возвращает токены доступа"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или несовпадение паролей"),
            @ApiResponse(responseCode = "409", description = "Пользователь с данным email уже существует")
    })
    @PostMapping("/registration")
    public Mono<ResponseEntity<TokenResponse>> registerUser(@Valid @RequestBody CreateUserRequest request) {
        return authService.registerUser(request)
                .map(token -> {
                    return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(
                            token.accessToken(),
                            token.expires_in(),
                            token.refresh_token(),
                            token.token_type(),
                            null
                    ));
                })
                .onErrorResume(UserAlreadyExistsException.class, e -> {
                    TokenResponse errorResponse = new TokenResponse(null,0,null,"",e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    TokenResponse errorResponse = new TokenResponse(null,0,null,"",e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
                });
    }

    @Operation(
            summary = "Авторизация пользователя",
            description = "Выполняет вход пользователя по email и паролю"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход, возвращает токены",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверный email или пароль")
    })
    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> loginUser(@Valid @RequestBody LoginRequest request) {
        return authService.loginUser(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)));
    }

    @Operation(
            summary = "Обновление токена",
            description = "Обновляет токен доступа по refresh токену"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Недействительный или истекший refresh токен")
    })
    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        System.out.println("Received refresh token request with token: " + request.refresh_token());

        return authService.refreshToken(request.refresh_token())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    System.err.println("Error during refresh token process: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null));
                });
    }

    @Operation(
            summary = "Получение информации о пользователе",
            description = "Возвращает информацию о пользователе по его ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно получена",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Недействительный или истекший токен доступа"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/me/{id}")
    public Mono<ResponseEntity<Map<String, Object>>> getUserInfo(@PathVariable String id,
                                                                 @RequestHeader("Authorization") String token) {
        return authService.getUserInfo(id, token.replace("Bearer ", ""))
                .map(userResponse -> ResponseEntity.ok(Map.of(
                        "id", userResponse.id(),
                        "username", userResponse.username(),
                        "email", userResponse.email(),
                        "roles", userResponse.roles(),
                        "created_at", userResponse.createdAt()
                )))
                .onErrorResume(e -> {
                    if (e instanceof UserNotFoundException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("message", e.getMessage())));
                    } else if (e instanceof AuthenticationException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", e.getMessage())));
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("message", "An unexpected error occurred")));
                });
    }
}
