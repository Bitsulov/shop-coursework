package com.example.shop.services;

import com.example.shop.dtos.auth.AuthResponse;
import com.example.shop.dtos.auth.RefreshRequest;
import com.example.shop.dtos.auth.SendCodeRequest;
import com.example.shop.dtos.auth.SignInRequest;
import com.example.shop.dtos.auth.SignUpRequest;
import com.example.shop.dtos.auth.VerifyRequest;

public interface AuthService {

    void register(SignUpRequest signUpRequest);

    void send(SendCodeRequest sendCodeRequest);

    AuthResponse verify(VerifyRequest verifyRequest);

    AuthResponse login(SignInRequest signInRequest);

    AuthResponse refresh(RefreshRequest refreshRequest);
}
