package com.example.shop.services;

import com.example.shop.dtos.order.OrderRequest;
import com.example.shop.dtos.order.OrderResponse;
import com.example.shop.security.UserPrincipal;

public interface OrderService {

    OrderResponse create(UserPrincipal principal, OrderRequest request);
}
