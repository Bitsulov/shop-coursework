package com.example.shop.dtos.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Характеристика товара")
public class ProductSpecResponse {

    @Schema(description = "Название характеристики", example = "Диагональ")
    private String name;

    @Schema(description = "Значение характеристики", example = "6,1″")
    private String value;
}
