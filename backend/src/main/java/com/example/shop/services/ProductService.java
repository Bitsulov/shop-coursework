package com.example.shop.services;

import com.example.shop.dtos.common.PageResponse;
import com.example.shop.dtos.product.ProductCardResponse;
import com.example.shop.dtos.product.ProductFilter;
import com.example.shop.dtos.product.ProductFiltersResponse;
import com.example.shop.dtos.product.ProductRequest;
import com.example.shop.dtos.product.ProductResponse;
import com.example.shop.dtos.product.ProductSort;
import com.example.shop.models.enums.CategoryEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    PageResponse<ProductCardResponse> search(ProductFilter filter, ProductSort sort, int page, int size);

    ProductFiltersResponse getFilters(CategoryEnum category);

    ProductResponse getByUuid(UUID uuid);

    List<ProductCardResponse> getRelated(UUID uuid);

    ProductResponse create(ProductRequest request, MultipartFile image);

    ProductResponse update(UUID uuid, ProductRequest request, MultipartFile image);

    void delete(UUID uuid);
}
