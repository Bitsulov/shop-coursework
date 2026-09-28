package com.example.shop.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Изменяемые данные пользователя")
public class UserUpdateDetails {

    @Schema(description = "Имя пользователя", example = "Иван")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be from 2 to 50 symbols")
    @Pattern(regexp = "^\\P{Cc}*$", message = "Name cannot contain control characters")
    private String name;

    @Schema(description = "Номер телефона", example = "+79991234567")
    @Pattern(regexp = "^\\+[1-9][0-9]{9,14}$", message = "Phone must be in international format: + and 10 to 15 digits")
    private String phone;

    @Schema(description = "Удалить текущий аватар", example = "false")
    private boolean deleteAvatar;
}
