package com.example.shop.mappers;

import com.example.shop.dtos.product.ProductCardResponse;
import com.example.shop.dtos.product.ProductRequest;
import com.example.shop.dtos.product.ProductResponse;
import com.example.shop.dtos.product.ProductSpecResponse;
import com.example.shop.models.entities.Product;
import com.example.shop.models.entities.ProductSpec;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    @Mapping(target = "category", source = "category.name")
    @Mapping(target = "brand", source = "brand.name")
    ProductResponse toResponse(Product product);

    ProductSpecResponse toSpecResponse(ProductSpec spec);

    ProductCardResponse toCardResponse(Product product);

    List<ProductCardResponse> toCardResponses(List<Product> products);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name")
    @Mapping(target = "summary")
    @Mapping(target = "description")
    @Mapping(target = "price")
    @Mapping(target = "oldPrice")
    @Mapping(target = "stock")
    Product toEntity(ProductRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name")
    @Mapping(target = "summary")
    @Mapping(target = "description")
    @Mapping(target = "price")
    @Mapping(target = "oldPrice")
    @Mapping(target = "stock")
    void updateEntity(ProductRequest request, @MappingTarget Product product);
}
