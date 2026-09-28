package com.example.shop.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Запрос на обновление токенов")
public class RefreshRequest {

    @Schema(description = "Refresh-токен")
    @NotBlank(message = "Refresh token is required")
    @Size(max = 1024, message = "Refresh token is too long")
    private String refreshToken;
}
