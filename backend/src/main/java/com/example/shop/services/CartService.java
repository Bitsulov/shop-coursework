package com.example.shop.services;

import com.example.shop.dtos.cart.CartQuantityRequest;
import com.example.shop.dtos.cart.CartResponse;
import com.example.shop.security.UserPrincipal;

import java.util.UUID;

public interface CartService {

    CartResponse get(UserPrincipal principal);

    CartResponse setQuantity(UserPrincipal principal, UUID productUuid, CartQuantityRequest request);

    CartResponse remove(UserPrincipal principal, UUID productUuid);
}
