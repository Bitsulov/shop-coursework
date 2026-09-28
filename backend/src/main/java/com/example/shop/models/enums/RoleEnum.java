package com.example.shop.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleEnum {
    USER(1, "user"),
    ADMIN(2, "admin");

    private final int id;

    @JsonValue
    private final String title;
}