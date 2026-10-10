package com.example.shop.services;

import com.example.shop.dtos.address.AddressRequest;
import com.example.shop.dtos.address.AddressResponse;
import com.example.shop.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    List<AddressResponse> getAll(UserPrincipal principal);

    AddressResponse getByUuid(UserPrincipal principal, UUID uuid);

    AddressResponse create(UserPrincipal principal, AddressRequest request);

    AddressResponse update(UserPrincipal principal, UUID uuid, AddressRequest request);

    AddressResponse select(UserPrincipal principal, UUID uuid);

    void delete(UserPrincipal principal, UUID uuid);
}
