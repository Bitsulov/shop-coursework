package com.example.shop.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(description = "JWT токены, выданные после регистрации, входа или обновления")
public class AuthResponse {

    @Schema(description = "UUID пользователя", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID uuid;

    @Schema(description = "Access-токен")
    private String accessToken;

    @Schema(description = "Refresh-токен")
    private String refreshToken;

    @Schema(description = "Время жизни access-токена в миллисекундах", example = "900000")
    private long accessTokenExpiresIn;

    @Schema(description = "Время жизни refresh-токена в миллисекундах", example = "2592000000")
    private long refreshTokenExpiresIn;
}
