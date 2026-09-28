package com.example.shop.dtos.mail;

public record VerificationCodeMessage(String email, String verificationCode) {
}
