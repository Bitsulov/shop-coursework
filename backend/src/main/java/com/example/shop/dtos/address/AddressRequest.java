package com.example.shop.dtos.address;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Данные для создания и изменения адреса доставки")
public class AddressRequest {

    @Schema(description = "Город", example = "Москва")
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot be longer than 100 symbols")
    @Pattern(regexp = "^\\P{Cc}*$", message = "City cannot contain control characters")
    private String city;

    @Schema(description = "Улица", example = "ул. Ленина")
    @NotBlank(message = "Street is required")
    @Size(max = 150, message = "Street cannot be longer than 150 symbols")
    @Pattern(regexp = "^\\P{Cc}*$", message = "Street cannot contain control characters")
    private String street;

    @Schema(description = "Дом, корпус, строение", example = "5к2")
    @NotBlank(message = "House is required")
    @Size(max = 20, message = "House cannot be longer than 20 symbols")
    @Pattern(regexp = "^\\P{Cc}*$", message = "House cannot contain control characters")
    private String house;

    @Schema(description = "Квартира или офис", example = "12")
    @Size(max = 20, message = "Apartment cannot be longer than 20 symbols")
    @Pattern(regexp = "^(?=.*\\S)\\P{Cc}*$", message = "Apartment cannot be blank or contain control characters")
    private String apartment;

    @Schema(description = "Почтовый индекс", example = "123456")
    @Pattern(regexp = "^[0-9]{6}$", message = "Postal code must be 6 digits")
    private String postalCode;

    @Schema(description = "Комментарий для курьера", example = "Домофон не работает, позвонить за 10 минут")
    @Size(max = 255, message = "Comment cannot be longer than 255 symbols")
    @Pattern(regexp = "^(?=.*\\S)\\P{Cc}*$", message = "Comment cannot be blank or contain control characters")
    private String comment;
}
