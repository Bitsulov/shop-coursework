package com.example.shop.dtos.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Оформленный заказ")
public class OrderResponse {

    @Schema(description = "UUID заказа", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID uuid;

    @Schema(description = "Имя получателя", example = "Анна Смирнова")
    private String recipientName;

    @Schema(description = "Телефон получателя", example = "+79123456789")
    private String recipientPhone;

    @Schema(description = "Адрес доставки", example = "Москва, ул. Лесная, д. 5к2, кв. 48")
    private String address;

    @Schema(description = "Товары заказа")
    private List<OrderProductResponse> products;

    @Schema(description = "Сумма без скидок", example = "101960.00")
    private BigDecimal subtotal;

    @Schema(description = "Размер скидки", example = "13000.00")
    private BigDecimal discount;

    @Schema(description = "Итого, списано с баланса", example = "88960.00")
    private BigDecimal total;

    @Schema(description = "Остаток на балансе после оплаты", example = "31040.00")
    private BigDecimal balance;

    @Schema(description = "Начало срока доставки", example = "2026-10-15T12:30:00Z")
    private Instant deliveryFrom;

    @Schema(description = "Конец срока доставки", example = "2026-10-16T12:30:00Z")
    private Instant deliveryTo;

    @Schema(description = "Дата оформления", example = "2026-10-13T12:30:00Z")
    private Instant createdAt;
}
