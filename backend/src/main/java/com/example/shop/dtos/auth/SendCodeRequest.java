package com.example.shop.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Запрос на повторную отправку кода подтверждения")
public class SendCodeRequest {

    @Schema(description = "Адрес электронной почты", example = "ivan@mail.ru")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 254, message = "Email cannot be longer than 254 symbols")
    private String email;
}
