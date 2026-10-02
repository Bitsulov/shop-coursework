package com.example.shop.services;

import com.example.shop.dtos.user.DeleteAccountRequest;
import com.example.shop.dtos.user.UserDetailResponse;
import com.example.shop.dtos.user.UserResponse;
import com.example.shop.dtos.user.UserUpdateDetails;
import com.example.shop.security.UserPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {

    UserDetailResponse getDetails(UserPrincipal principal);

    UserDetailResponse updateDetails(UserPrincipal principal, UserUpdateDetails details, MultipartFile avatar);

    UserResponse getByUuid(UUID uuid);

    void delete(UserPrincipal principal, DeleteAccountRequest request);
}