package com.example.shop.dtos.product;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductSort {
    NEW("new"),
    PRICE_ASC("price_asc"),
    PRICE_DESC("price_desc"),
    DISCOUNT("discount");

    @JsonValue
    private final String title;
}
