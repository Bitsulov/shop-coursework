package com.example.shop.dtos.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Адрес доставки пользователя")
public class AddressResponse {

    @Schema(description = "UUID адреса", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID uuid;

    @Schema(description = "Город", example = "Москва")
    private String city;

    @Schema(description = "Улица", example = "ул. Ленина")
    private String street;

    @Schema(description = "Дом, корпус, строение", example = "5к2")
    private String house;

    @Schema(description = "Квартира или офис", example = "12")
    private String apartment;

    @Schema(description = "Почтовый индекс", example = "123456")
    private String postalCode;

    @Schema(description = "Комментарий для курьера", example = "Домофон не работает, позвонить за 10 минут")
    private String comment;

    @Schema(description = "Признак выбранного адреса", example = "true")
    private boolean selected;
}
