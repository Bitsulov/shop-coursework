package com.example.shop.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryEnum {
    PHONES(1, "phones"),
    LAPTOPS(2, "laptops"),
    TV(3, "tv"),
    LARGE_APPLIANCES(4, "large_appliances"),
    SMALL_APPLIANCES(5, "small_appliances"),
    AUDIO(6, "audio");

    private final int id;

    @JsonValue
    private final String title;
}
