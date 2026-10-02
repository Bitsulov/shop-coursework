package com.example.shop.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Подтверждение удаления аккаунта")
public class DeleteAccountRequest {

    @Schema(description = "Текущий пароль", example = "StrongPass123")
    @NotBlank(message = "Password is required")
    @Size(max = 72, message = "Password cannot be longer than 72 symbols")
    private String password;
}
