package com.example.shop.controllers;

import com.example.shop.dtos.cart.CartQuantityRequest;
import com.example.shop.dtos.cart.CartResponse;
import com.example.shop.security.UserPrincipal;
import com.example.shop.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Корзина", description = "Корзина текущего пользователя")
public class CartController {

    private final CartService cartService;

    @Operation(
            summary = "Корзина",
            description = "Возвращает товары в корзине текущего пользователя и итоговые суммы.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Корзина получена"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping
    public ResponseEntity<CartResponse> get(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal
    ) {
        CartResponse response = cartService.get(principal);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Количество товара в корзине",
            description = "Задаёт количество товара в корзине и добавляет товар, если его там ещё нет. В корзине может быть не больше 50 разных товаров.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Корзина изменена"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или неверный формат UUID"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь или товар не найден"),
                    @ApiResponse(responseCode = "409", description = "Недостаточно товара на складе или достигнут лимит корзины"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PutMapping("/products/{productUuid}")
    public ResponseEntity<CartResponse> setQuantity(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "UUID товара", required = true)
            @PathVariable UUID productUuid,
            @Parameter(description = "Количество", required = true)
            @Valid @RequestBody CartQuantityRequest request
    ) {
        CartResponse response = cartService.setQuantity(principal, productUuid, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Удаление товара из корзины",
            description = "Убирает товар из корзины текущего пользователя.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Товар убран из корзины"),
                    @ApiResponse(responseCode = "400", description = "Неверный формат UUID"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь или товар не найден, или товара нет в корзине"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @DeleteMapping("/products/{productUuid}")
    public ResponseEntity<CartResponse> remove(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "UUID товара", required = true)
            @PathVariable UUID productUuid
    ) {
        CartResponse response = cartService.remove(principal, productUuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
