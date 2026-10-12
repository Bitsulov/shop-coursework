package com.example.shop.dtos.product;

import com.example.shop.models.enums.CategoryEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Полные данные товара")
public class ProductResponse {

    @Schema(description = "UUID товара", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID uuid;

    @Schema(description = "Название", example = "Смартфон Nord X1 128 ГБ, графит")
    private String name;

    @Schema(description = "Краткие характеристики для карточки", example = "6,1″ OLED · 128 ГБ · камера 48 Мп")
    private String summary;

    @Schema(description = "Описание", example = "Компактный смартфон с OLED-экраном и камерой 48 Мп.")
    private String description;

    @Schema(description = "Категория", example = "phones")
    private CategoryEnum category;

    @Schema(description = "Название бренда", example = "Nord")
    private String brand;

    @Schema(description = "Цвет корпуса", example = "Чёрный")
    private String color;

    @Schema(description = "Цена", example = "54990.00")
    private BigDecimal price;

    @Schema(description = "Цена до скидки", example = "61990.00")
    private BigDecimal oldPrice;

    @Schema(description = "Остаток на складе", example = "12")
    private int stock;

    @Schema(description = "Ссылка на фото", example = "http://localhost/storage/shop-files/client_7c9e6679-7425-40de-944b-e07fc1f90ae7.jpg")
    private String imageUrl;

    @Schema(description = "Характеристики в порядке показа")
    private List<ProductSpecResponse> specs;

    @Schema(description = "Дата добавления", example = "2026-10-12T12:30:00Z")
    private Instant createdAt;

    @Schema(description = "Дата последнего изменения", example = "2026-10-12T12:30:00Z")
    private Instant updatedAt;
}
