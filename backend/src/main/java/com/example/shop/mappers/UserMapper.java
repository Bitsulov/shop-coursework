package com.example.shop.mappers;

import com.example.shop.dtos.auth.SignUpRequest;
import com.example.shop.dtos.user.UserDetailResponse;
import com.example.shop.dtos.user.UserResponse;
import com.example.shop.dtos.user.UserUpdateDetails;
import com.example.shop.models.entities.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "role", source = "role.name")
    UserDetailResponse toDetailResponse(User user);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name")
    @Mapping(target = "phone")
    User toEntity(SignUpRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name")
    @Mapping(target = "phone")
    void updateEntity(UserUpdateDetails details, @MappingTarget User user);
}
