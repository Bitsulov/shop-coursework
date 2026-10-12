package com.example.shop.dtos.product;

import com.example.shop.models.enums.CategoryEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
@Setter
@Schema(description = "Данные для создания и изменения товара")
public class ProductRequest {

    @Schema(description = "Название", example = "Смартфон Nord X1 128 ГБ, графит")
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name cannot be longer than 200 symbols")
    @Pattern(regexp = "^\\P{Cc}*$", message = "Name cannot contain control characters")
    private String name;

    @Schema(description = "Краткие характеристики для карточки", example = "6,1″ OLED · 128 ГБ · камера 48 Мп")
    @NotBlank(message = "Summary is required")
    @Size(max = 255, message = "Summary cannot be longer than 255 symbols")
    @Pattern(regexp = "^\\P{Cc}*$", message = "Summary cannot contain control characters")
    private String summary;

    @Schema(description = "Описание", example = "Компактный смартфон с OLED-экраном и камерой 48 Мп.")
    @Size(max = 5000, message = "Description cannot be longer than 5000 symbols")
    @Pattern(regexp = "(?s)^(?=.*\\S)[\\P{Cc}\\n\\r\\t]*$", message = "Description cannot be blank or contain control characters")
    private String description;

    @Schema(description = "Категория", example = "phones")
    @NotNull(message = "Category is required")
    private CategoryEnum category;

    @Schema(description = "Название бренда", example = "Nord")
    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand cannot be longer than 50 symbols")
    @Pattern(regexp = "^\\P{Cc}*$", message = "Brand cannot contain control characters")
    private String brand;

    @Schema(description = "Цвет корпуса", example = "Чёрный")
    @Size(max = 50, message = "Color cannot be longer than 50 symbols")
    @Pattern(regexp = "^(?=.*\\S)\\P{Cc}*$", message = "Color cannot be blank or contain control characters")
    private String color;

    @Schema(description = "Цена", example = "54990.00")
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Price must have up to 10 digits and 2 decimals")
    private BigDecimal price;

    @Schema(description = "Цена до скидки", example = "61990.00")
    @Positive(message = "Old price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Old price must have up to 10 digits and 2 decimals")
    private BigDecimal oldPrice;

    @Schema(description = "Остаток на складе", example = "12")
    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    @Max(value = 1_000_000, message = "Stock cannot be more than 1000000")
    private Integer stock;

    @Schema(description = "Характеристики в порядке показа")
    @NotNull(message = "Specs are required")
    @Size(max = 50, message = "Product cannot have more than 50 specs")
    private List<@NotNull(message = "Spec is required") @Valid ProductSpecRequest> specs = new ArrayList<>();

    @Schema(description = "Удалить текущее фото", example = "false")
    private boolean deleteImage;

    @Schema(hidden = true)
    @AssertTrue(message = "Old price must be greater than price")
    public boolean isOldPriceGreaterThanPrice() {
        return oldPrice == null || price == null || oldPrice.compareTo(price) > 0;
    }

    @Schema(hidden = true)
    @AssertTrue(message = "Spec names must be unique")
    public boolean isSpecNamesUnique() {
        if (specs == null) {
            return true;
        }
        long distinct = specs.stream()
                .filter(Objects::nonNull)
                .map(ProductSpecRequest::getName)
                .filter(Objects::nonNull)
                .map(name -> name.strip().toLowerCase(Locale.ROOT))
                .distinct()
                .count();
        return distinct == specs.stream().filter(spec -> spec != null && spec.getName() != null).count();
    }
}
