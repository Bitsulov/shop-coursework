package com.example.shop.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Подтверждение адреса электронной почты")
public class VerifyRequest {

    @Schema(description = "Адрес электронной почты", example = "ivan@mail.ru")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 254, message = "Email cannot be longer than 254 symbols")
    private String email;

    @Schema(description = "Код подтверждения из письма", example = "483920")
    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Verification code must contain 6 digits")
    private String verificationCode;
}
