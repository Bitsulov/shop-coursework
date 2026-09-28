package com.example.shop.exceptions.models;

import java.time.Instant;

public record AppError(int statusCode, String message, String path, Instant timestamp) {
}
