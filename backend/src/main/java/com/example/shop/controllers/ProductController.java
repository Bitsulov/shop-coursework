package com.example.shop.controllers;

import com.example.shop.dtos.product.ProductRequest;
import com.example.shop.dtos.product.ProductResponse;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Товары", description = "Просмотр товара, создание, изменение и удаление товаров администратором")
public class ProductController {

    private final ProductService productService;

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
