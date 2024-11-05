package org.samatov.individuals_api.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.samatov.individuals_api.config.TestSecurityConfig;
import org.samatov.individuals_api.dto.*;
import org.samatov.individuals_api.exception.UserAlreadyExistsException;
import org.samatov.individuals_api.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@Import({TestSecurityConfig.class})
@ActiveProfiles("test")
public class AuthServiceImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Регистрация пользователя: успешное создание")
    public void givenCreateUserRequest_whenRegisterUser_thenReturnTokenResponse() {
        // given
        CreateUserRequest request = new CreateUserRequest("John", "Doe", "john.doe@example.com", "password", "password");
        TokenResponse tokenResponse = new TokenResponse("mockAccessToken", 3600, "mockRefreshToken", "Bearer",null);

        BDDMockito.given(authService.registerUser(any(CreateUserRequest.class)))
                .willReturn(Mono.just(tokenResponse));

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), CreateUserRequest.class)
                .exchange();

        // then
        result.expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.access_token").isEqualTo("mockAccessToken")
                .jsonPath("$.refresh_token").isEqualTo("mockRefreshToken");
    }

    @Test
    @DisplayName("Авторизация пользователя: успешный вход")
    public void givenLoginRequest_whenLoginUser_thenReturnTokenResponse() {
        // given
        LoginRequest loginRequest = new LoginRequest("john.doe@example.com", "password");
        TokenResponse tokenResponse = new TokenResponse("mockAccessToken", 3600, "mockRefreshToken", "Bearer",null);

        BDDMockito.given(authService.loginUser(any(LoginRequest.class)))
                .willReturn(Mono.just(tokenResponse));

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(loginRequest), LoginRequest.class)
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.access_token").isEqualTo("mockAccessToken")
                .jsonPath("$.expires_in").isEqualTo(3600)
                .jsonPath("$.refresh_token").isEqualTo("mockRefreshToken")
                .jsonPath("$.token_type").isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Обновление токена: успешное выполнение")
    public void givenRefreshToken_whenRefreshToken_thenReturnTokenResponse() {
        // given
        String refreshTokenValue = "mockRefreshToken";
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("client_id","client_secret",refreshTokenValue,"grant_type");
        TokenResponse tokenResponse = new TokenResponse("newMockAccessToken", 3600, "newMockRefreshToken", "Bearer",null);

        BDDMockito.given(authService.refreshToken(any(String.class)))
                .willReturn(Mono.just(tokenResponse));

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(refreshTokenRequest), RefreshTokenRequest.class)
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.access_token").isEqualTo("newMockAccessToken")
                .jsonPath("$.expires_in").isEqualTo(3600)
                .jsonPath("$.refresh_token").isEqualTo("newMockRefreshToken")
                .jsonPath("$.token_type").isEqualTo("Bearer");
    }
    @Test
    @DisplayName("Получение информации о пользователе: успешный запрос")
    public void givenUserIdAndAccessToken_whenGetUserInfo_thenReturnUserResponse() {
        // given
        String userId = "12345";
        String accessToken = "mockAccessToken";
        UserResponse userResponse = new UserResponse(userId, "john.doe", "john.doe@example.com", List.of("USER"), "2024-11-05");

        BDDMockito.given(authService.getUserInfo(eq(userId), eq(accessToken)))
                .willReturn(Mono.just(userResponse));

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/v1/auth/me/" + userId)
                .header("Authorization", "Bearer " + accessToken)
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("12345")
                .jsonPath("$.username").isEqualTo("john.doe")
                .jsonPath("$.email").isEqualTo("john.doe@example.com")
                .jsonPath("$.roles[0]").isEqualTo("USER")
                .jsonPath("$.created_at").isEqualTo("2024-11-05");
    }


    @Test
    @DisplayName("Регистрация пользователя: ошибка при существующем пользователе (UserAlreadyExistsException)")
    public void givenExistingUser_whenRegisterUser_thenReturnConflictStatus() {
        // given
        CreateUserRequest request = new CreateUserRequest("John", "Doe", "john.doe@example.com", "password", "password");

        BDDMockito.given(authService.registerUser(any(CreateUserRequest.class)))
                .willReturn(Mono.error(new UserAlreadyExistsException("User with this email already exists")));

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), CreateUserRequest.class)
                .exchange();

        // then
        result.expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("User with this email already exists");
    }

    @Test
    @DisplayName("Получение информации о пользователе: ошибка при ненайденном пользователе (UserNotFoundException)")
    public void givenInvalidUserId_whenGetUserInfo_thenReturnNotFoundStatus() {
        // given
        String userId = "invalidUserId";
        String accessToken = "mockAccessToken";

        BDDMockito.given(authService.getUserInfo(eq(userId), eq(accessToken)))
                .willReturn(Mono.error(new UserNotFoundException("User not found")));

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/v1/auth/me/" + userId)
                .header("Authorization", "Bearer " + accessToken)
                .exchange();

        // then
        result.expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User not found");
    }

}
