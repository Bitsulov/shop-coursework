package com.example.shop.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Публичные данные пользователя")
public class UserResponse {

    @Schema(description = "UUID пользователя", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID uuid;

    @Schema(description = "Имя пользователя", example = "Иван")
    private String name;

    @Schema(description = "Ссылка на аватар", example = "http://localhost:9000/shop-files/images/7c9e6679-7425-40de-944b-e07fc1f90ae7.jpg")
    private String avatarUrl;

    @Schema(description = "Дата регистрации", example = "2026-09-22T12:30:00Z")
    private Instant createdAt;
}
