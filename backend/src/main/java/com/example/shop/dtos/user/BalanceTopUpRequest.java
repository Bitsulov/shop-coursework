package com.example.shop.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Пополнение баланса банковской картой. Учебная оплата, деньги не списываются")
public class BalanceTopUpRequest {

    @Schema(description = "Сумма в рублях", example = "5000")
    @NotNull(message = "Amount is required")
    @Min(value = 100, message = "Amount must be from 100 to 500000")
    @Max(value = 500_000, message = "Amount must be from 100 to 500000")
    private Integer amount;

    @Schema(description = "Номер карты, 16 цифр без пробелов", example = "4276160012345678")
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be 16 digits")
    private String cardNumber;

    @Schema(description = "Срок действия карты в формате ММ/ГГ", example = "08/28")
    @NotBlank(message = "Card expiry is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Card expiry must be in MM/YY format")
    private String expiry;

    @Schema(description = "CVC, 3 цифры", example = "123")
    @NotBlank(message = "CVC is required")
    @Pattern(regexp = "^[0-9]{3}$", message = "CVC must be 3 digits")
    private String cvc;
}
