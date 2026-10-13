package com.example.shop.dtos.cart;

import com.example.shop.dtos.product.ProductCardResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Товар в корзине")
public class CartProductResponse {

    @Schema(description = "Товар")
    private ProductCardResponse product;

    @Schema(description = "Количество", example = "2")
    private int quantity;

    @Schema(description = "Сумма по товару с учётом скидки", example = "109980.00")
    private BigDecimal total;
}
