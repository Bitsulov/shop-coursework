package com.example.shop.dtos.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema(description = "Корзина текущего пользователя")
public class CartResponse {

    @Schema(description = "Товары, новые первыми")
    private List<CartProductResponse> products;

    @Schema(description = "Всего штук", example = "3")
    private int totalQuantity;

    @Schema(description = "Сумма без скидок", example = "139970.00")
    private BigDecimal subtotal;

    @Schema(description = "Размер скидки", example = "20000.00")
    private BigDecimal discount;

    @Schema(description = "Итого", example = "119970.00")
    private BigDecimal total;
}
