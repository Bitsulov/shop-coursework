package com.example.shop.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerificationCodesType {
    REGISTER(1, "register");

    private final int id;

    @JsonValue
    private final String title;
}
