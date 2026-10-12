package com.example.shop.configs;

import com.example.shop.dtos.product.ProductSort;
import com.example.shop.models.enums.CategoryEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.function.Function;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, CategoryEnum.class,
                source -> byTitle(CategoryEnum.values(), CategoryEnum::getTitle, source));
        registry.addConverter(String.class, ProductSort.class,
                source -> byTitle(ProductSort.values(), ProductSort::getTitle, source));
    }

    private static <T> T byTitle(T[] values, Function<T, String> title, String source) {
        return Arrays.stream(values)
                .filter(value -> title.apply(value).equals(source))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown value: " + source));
    }
}
