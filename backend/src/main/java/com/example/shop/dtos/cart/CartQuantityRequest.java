package com.example.shop.dtos.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Количество товара в корзине")
public class CartQuantityRequest {

    @Schema(description = "Количество", example = "2")
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Max(value = 1_000_000, message = "Quantity cannot be more than 1000000")
    private Integer quantity;
}
