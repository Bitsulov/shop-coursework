package com.example.shop.dtos.user;

import com.example.shop.models.enums.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Полные данные пользователя, доступные владельцу и администратору")
public class UserDetailResponse {

    @Schema(description = "UUID пользователя", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID uuid;

    @Schema(description = "Имя пользователя", example = "Иван")
    private String name;

    @Schema(description = "Ссылка на аватар", example = "http://localhost:9000/shop-files/images/7c9e6679-7425-40de-944b-e07fc1f90ae7.jpg")
    private String avatarUrl;

    @Schema(description = "Адрес электронной почты", example = "ivan@mail.ru")
    private String email;

    @Schema(description = "Номер телефона", example = "+79991234567")
    private String phone;

    @Schema(description = "Баланс", example = "1500.00")
    private BigDecimal balance;

    @Schema(description = "Роль", example = "user")
    private RoleEnum role;

    @Schema(description = "Признак активного аккаунта", example = "true")
    private boolean active;

    @Schema(description = "Признак заблокированного аккаунта", example = "false")
    private boolean blocked;

    @Schema(description = "Дата регистрации", example = "2026-09-22T12:30:00Z")
    private Instant createdAt;

    @Schema(description = "Дата последнего изменения данных пользователя", example = "2026-09-22T12:30:00Z")
    private Instant updatedAt;
}