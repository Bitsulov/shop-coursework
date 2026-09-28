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
@Schema(description = "Данные для регистрации пользователя")
public class SignUpRequest {

    @Schema(description = "Адрес электронной почты", example = "ivan@mail.ru")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 254, message = "Email cannot be longer than 254 symbols")
    private String email;

    @Schema(description = "Пароль: от 8 до 72 символов, хотя бы одна буква и одна цифра", example = "StrongPass123")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be from 8 to 72 symbols")
    @Pattern(regexp = "^(?=.*\\p{L})(?=.*\\d).+$", message = "Password must contain at least one letter and one digit")
    private String password;

    @Schema(description = "Повтор пароля", example = "StrongPass123")
    @NotBlank(message = "Password confirmation is required")
    @Size(max = 72, message = "Password confirmation cannot be longer than 72 symbols")
    private String confirmPassword;

    @Schema(description = "Имя пользователя", example = "Иван")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be from 2 to 50 symbols")
    @Pattern(regexp = "^\\P{Cc}*$", message = "Name cannot contain control characters")
    private String name;

    @Schema(description = "Номер телефона", example = "+79991234567")
    @Pattern(regexp = "^\\+[1-9][0-9]{9,14}$", message = "Phone must be in international format: + and 10 to 15 digits")
    private String phone;
}
