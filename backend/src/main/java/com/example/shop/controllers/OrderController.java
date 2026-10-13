package com.example.shop.controllers;

import com.example.shop.dtos.order.OrderRequest;
import com.example.shop.dtos.order.OrderResponse;
import com.example.shop.security.UserPrincipal;
import com.example.shop.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Заказы", description = "Оформление заказа из корзины")
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Оформление заказа",
            description = "Оформляет заказ из корзины текущего пользователя с оплатой с баланса. Списывает остатки товаров и баланс, очищает корзину.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Заказ оформлен"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь или адрес не найден"),
                    @ApiResponse(responseCode = "409", description = "Корзина пуста, товара недостаточно на складе или не хватает баланса"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Получатель и адрес доставки", required = true)
            @Valid @RequestBody OrderRequest request
    ) {
        OrderResponse response = orderService.create(principal, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
