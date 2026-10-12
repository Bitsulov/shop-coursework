package com.example.shop.controllers;

import com.example.shop.dtos.common.PageResponse;
import com.example.shop.dtos.product.ProductCardResponse;
import com.example.shop.dtos.product.ProductFilter;
import com.example.shop.dtos.product.ProductFiltersResponse;
import com.example.shop.dtos.product.ProductRequest;
import com.example.shop.dtos.product.ProductResponse;
import com.example.shop.dtos.product.ProductSort;
import com.example.shop.models.enums.CategoryEnum;
import com.example.shop.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Товары", description = "Каталог, просмотр товара, создание, изменение и удаление товаров администратором")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Каталог товаров",
            description = "Возвращает страницу карточек товаров с фильтрами и сортировкой.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Страница получена"),
                    @ApiResponse(responseCode = "400", description = "Неверное значение параметра"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping
    public ResponseEntity<PageResponse<ProductCardResponse>> search(
            @Parameter(description = "Категория", example = "phones")
            @RequestParam(required = false) CategoryEnum category,
            @Parameter(description = "Бренды, можно передать несколько", example = "Nord")
            @RequestParam(name = "brand", required = false) List<String> brands,
            @Parameter(description = "Цвета, можно передать несколько", example = "Чёрный")
            @RequestParam(name = "color", required = false) List<String> colors,
            @Parameter(description = "Цена от", example = "20000")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Цена до", example = "60000")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Только товары в наличии")
            @RequestParam(defaultValue = "false") boolean inStock,
            @Parameter(description = "Только товары со скидкой")
            @RequestParam(defaultValue = "false") boolean sale,
            @Parameter(description = "Сортировка", example = "price_asc")
            @RequestParam(defaultValue = "new") ProductSort sort,
            @Parameter(description = "Номер страницы, начиная с нуля", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы, от 1 до 48", example = "24")
            @RequestParam(defaultValue = "24") int size
    ) {
        ProductFilter filter = ProductFilter.builder()
                .category(category)
                .brands(brands)
                .colors(colors)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .inStock(inStock)
                .sale(sale)
                .build();
        PageResponse<ProductCardResponse> response = productService.search(filter, sort, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Варианты фильтров",
            description = "Возвращает бренды и цвета, которые есть у товаров. Категория сужает списки до её товаров.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Варианты получены"),
                    @ApiResponse(responseCode = "400", description = "Неверное значение категории"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping("/filters")
    public ResponseEntity<ProductFiltersResponse> getFilters(
            @Parameter(description = "Категория", example = "phones")
            @RequestParam(required = false) CategoryEnum category
    ) {
        ProductFiltersResponse response = productService.getFilters(category);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Похожие товары",
            description = "Возвращает до четырёх самых новых товаров той же категории.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Товары получены"),
                    @ApiResponse(responseCode = "400", description = "Неверный формат UUID"),
                    @ApiResponse(responseCode = "404", description = "Товар не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping("/{uuid}/related")
    public ResponseEntity<List<ProductCardResponse>> getRelated(
            @Parameter(description = "UUID товара", required = true)
            @PathVariable UUID uuid
    ) {
        List<ProductCardResponse> response = productService.getRelated(uuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Товар по UUID",
            description = "Возвращает полные данные товара вместе с характеристиками.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Товар получен"),
                    @ApiResponse(responseCode = "400", description = "Неверный формат UUID"),
                    @ApiResponse(responseCode = "404", description = "Товар не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping("/{uuid}")
    public ResponseEntity<ProductResponse> getByUuid(
            @Parameter(description = "UUID товара", required = true)
            @PathVariable UUID uuid
    ) {
        ProductResponse response = productService.getByUuid(uuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Создание товара",
            description = "Создаёт товар и загружает его фото. Бренд, которого ещё нет, создаётся по названию. Доступно только администратору.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Товар создан"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или неверное изображение"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
                    @ApiResponse(responseCode = "413", description = "Файл слишком большой"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> create(
            @Parameter(description = "Данные товара", required = true)
            @Valid @RequestPart("details") ProductRequest details,
            @Parameter(description = "Фото товара")
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        ProductResponse response = productService.create(details, image);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Изменение товара",
            description = "Заменяет данные и характеристики товара, загружает новое фото или удаляет старое. Доступно только администратору.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Товар изменён"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации, неверное изображение или одновременно новое фото и его удаление"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
                    @ApiResponse(responseCode = "404", description = "Товар не найден"),
                    @ApiResponse(responseCode = "413", description = "Файл слишком большой"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{uuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> update(
            @Parameter(description = "UUID товара", required = true)
            @PathVariable UUID uuid,
            @Parameter(description = "Новые данные товара", required = true)
            @Valid @RequestPart("details") ProductRequest details,
            @Parameter(description = "Новое фото товара")
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        ProductResponse response = productService.update(uuid, details, image);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Удаление товара",
            description = "Удаляет товар, его характеристики и фото. Доступно только администратору.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Товар удалён"),
                    @ApiResponse(responseCode = "400", description = "Неверный формат UUID"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
                    @ApiResponse(responseCode = "404", description = "Товар не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID товара", required = true)
            @PathVariable UUID uuid
    ) {
        productService.delete(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
