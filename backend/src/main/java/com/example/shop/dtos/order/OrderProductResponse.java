package com.example.shop.dtos.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Товар в заказе")
public class OrderProductResponse {

    @Schema(description = "Название на момент покупки", example = "Смартфон Nord X1 128 ГБ, графит")
    private String name;

    @Schema(description = "Цена на момент покупки", example = "54990.00")
    private BigDecimal price;

    @Schema(description = "Цена до скидки на момент покупки", example = "61990.00")
    private BigDecimal oldPrice;

    @Schema(description = "Количество", example = "1")
    private int quantity;
}
