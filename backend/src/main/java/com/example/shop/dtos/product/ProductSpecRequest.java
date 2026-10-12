package com.example.shop.dtos.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Характеристика товара")
public class ProductSpecRequest {

    @Schema(description = "Название характеристики", example = "Диагональ")
    @NotBlank(message = "Spec name is required")
    @Size(max = 100, message = "Spec name cannot be longer than 100 symbols")
    @Pattern(regexp = "^\\P{Cc}*$", message = "Spec name cannot contain control characters")
    private String name;

    @Schema(description = "Значение характеристики", example = "6,1″")
    @NotBlank(message = "Spec value is required")
    @Size(max = 255, message = "Spec value cannot be longer than 255 symbols")
    @Pattern(regexp = "^\\P{Cc}*$", message = "Spec value cannot contain control characters")
    private String value;
}
