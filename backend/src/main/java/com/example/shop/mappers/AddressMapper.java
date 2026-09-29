package com.example.shop.mappers;

import com.example.shop.dtos.address.AddressRequest;
import com.example.shop.dtos.address.AddressResponse;
import com.example.shop.models.entities.Address;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressMapper {

    AddressResponse toResponse(Address address);

    List<AddressResponse> toResponses(List<Address> addresses);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "city")
    @Mapping(target = "street")
    @Mapping(target = "house")
    @Mapping(target = "apartment")
    @Mapping(target = "postalCode")
    @Mapping(target = "comment")
    Address toEntity(AddressRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "city")
    @Mapping(target = "street")
    @Mapping(target = "house")
    @Mapping(target = "apartment")
    @Mapping(target = "postalCode")
    @Mapping(target = "comment")
    void updateEntity(AddressRequest request, @MappingTarget Address address);
}
