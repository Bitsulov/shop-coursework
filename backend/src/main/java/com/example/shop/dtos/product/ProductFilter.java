package com.example.shop.dtos.product;

import com.example.shop.models.enums.CategoryEnum;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ProductFilter {

    private CategoryEnum category;
    private List<String> brands;
    private List<String> colors;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private boolean inStock;
    private boolean sale;
}
