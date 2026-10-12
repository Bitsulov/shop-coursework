package com.example.shop.services;

import com.example.shop.dtos.product.ProductRequest;
import com.example.shop.dtos.product.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProductService {

    ProductResponse getByUuid(UUID uuid);

    ProductResponse create(ProductRequest request, MultipartFile image);

    ProductResponse update(UUID uuid, ProductRequest request, MultipartFile image);

    void delete(UUID uuid);
}
