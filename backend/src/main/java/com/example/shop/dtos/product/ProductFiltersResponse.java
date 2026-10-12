package com.example.shop.dtos.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Варианты для фильтров каталога")
public class ProductFiltersResponse {

    @Schema(description = "Бренды товаров по алфавиту", example = "[\"Lumen\", \"Nord\"]")
    private List<String> brands;

    @Schema(description = "Цвета товаров по алфавиту", example = "[\"Серебристый\", \"Чёрный\"]")
    private List<String> colors;
}
