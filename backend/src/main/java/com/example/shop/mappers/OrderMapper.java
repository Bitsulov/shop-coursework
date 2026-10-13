package com.example.shop.mappers;

import com.example.shop.dtos.order.OrderProductResponse;
import com.example.shop.dtos.order.OrderResponse;
import com.example.shop.models.entities.Order;
import com.example.shop.models.entities.OrderProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    @Mapping(target = "balance", ignore = true)
    OrderResponse toResponse(Order order);

    OrderProductResponse toProductResponse(OrderProduct product);
}
