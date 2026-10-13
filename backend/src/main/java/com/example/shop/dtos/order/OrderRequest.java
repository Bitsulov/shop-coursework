package com.example.shop.dtos.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Данные для оформления заказа")
public class OrderRequest {

    @Schema(description = "Имя получателя", example = "Анна Смирнова")
    @NotBlank(message = "Recipient name is required")
    @Size(min = 2, max = 50, message = "Recipient name must be from 2 to 50 symbols")
    @Pattern(regexp = "^\\P{Cc}*$", message = "Recipient name cannot contain control characters")
    private String recipientName;

    @Schema(description = "Телефон получателя", example = "+79123456789")
    @NotBlank(message = "Recipient phone is required")
    @Pattern(regexp = "^\\+[1-9][0-9]{9,14}$", message = "Recipient phone must be in international format: + and 10 to 15 digits")
    private String recipientPhone;

    @Schema(description = "UUID адреса доставки из адресов пользователя", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull(message = "Address is required")
    private UUID addressUuid;
}
