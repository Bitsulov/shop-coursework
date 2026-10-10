package com.example.shop.services.impl;

import com.example.shop.dtos.address.AddressRequest;
import com.example.shop.dtos.address.AddressResponse;
import com.example.shop.exceptions.ConflictException;
import com.example.shop.exceptions.ResourceNotFoundException;
import com.example.shop.mappers.AddressMapper;
import com.example.shop.models.entities.Address;
import com.example.shop.models.entities.User;
import com.example.shop.repositories.AddressRepository;
import com.example.shop.repositories.UserRepository;
import com.example.shop.security.UserPrincipal;
import com.example.shop.services.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private static final int MAX_ADDRESSES = 10;

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAll(UserPrincipal principal) {
        UUID userUuid = principal.getUuid();

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("Getting addresses failed: user not found, uuid={}", userUuid);
                    return new ResourceNotFoundException("User not found");
                });

        List<Address> addresses = addressRepository.findAllByUserOrderBySelectedDescCreatedAtDesc(user);

        return addressMapper.toResponses(addresses);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getByUuid(UserPrincipal principal, UUID uuid) {
        UUID userUuid = principal.getUuid();

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("Getting address failed: user not found, uuid={}", userUuid);
                    return new ResourceNotFoundException("User not found");
                });
        Address address = addressRepository.findByUuidAndUser(uuid, user)
                .orElseThrow(() -> {
                    log.warn("Getting address failed: address not found, user uuid={}, address uuid={}", userUuid, uuid);
                    return new ResourceNotFoundException("Address not found");
                });

        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse create(UserPrincipal principal, AddressRequest request) {
        UUID userUuid = principal.getUuid();

        User user = userRepository.findLockedByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("Address creation failed: user not found, uuid={}", userUuid);
                    return new ResourceNotFoundException("User not found");
                });
        if (!user.isActive()) {
            log.warn("Address creation failed: user is deleted or not verified, uuid={}", userUuid);
            throw new ResourceNotFoundException("User not found");
        }

        long count = addressRepository.countByUser(user);
        if (count >= MAX_ADDRESSES) {
            log.warn("Address creation warn: limit of {} addresses reached, user uuid={}", MAX_ADDRESSES, userUuid);
            throw new ConflictException("Address limit reached");
        }

        Address address = addressMapper.toEntity(request);
        address.setUuid(UUID.randomUUID());
        address.setUser(user);
        address.setSelected(count == 0);
        address.setCreatedAt(Instant.now());
        log.debug("Prepared address for saving, selected={}, city={}, street={}, house={}, address uuid={}",
                address.isSelected(), address.getCity(), address.getStreet(), address.getHouse(), address.getUuid());
        addressRepository.save(address);

        log.info("User created address, user uuid={}, address uuid={}", userUuid, address.getUuid());
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse update(UserPrincipal principal, UUID uuid, AddressRequest request) {
        UUID userUuid = principal.getUuid();

        User user = userRepository.findLockedByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("Address update failed: user not found, uuid={}", userUuid);
                    return new ResourceNotFoundException("User not found");
                });
        if (!user.isActive()) {
            log.warn("Address update failed: user is deleted or not verified, uuid={}", userUuid);
            throw new ResourceNotFoundException("User not found");
        }
        Address address = addressRepository.findByUuidAndUser(uuid, user)
                .orElseThrow(() -> {
                    log.warn("Address update failed: address not found, user uuid={}, address uuid={}", userUuid, uuid);
                    return new ResourceNotFoundException("Address not found");
                });

        addressMapper.updateEntity(request, address);
        log.debug("Updated address data, city={}, street={}, house={}, address uuid={}",
                address.getCity(), address.getStreet(), address.getHouse(), uuid);

        log.info("User updated address, user uuid={}, address uuid={}", userUuid, uuid);
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse select(UserPrincipal principal, UUID uuid) {
        UUID userUuid = principal.getUuid();

        User user = userRepository.findLockedByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("Address selection failed: user not found, uuid={}", userUuid);
                    return new ResourceNotFoundException("User not found");
                });
        if (!user.isActive()) {
            log.warn("Address selection failed: user is deleted or not verified, uuid={}", userUuid);
            throw new ResourceNotFoundException("User not found");
        }
        Address address = addressRepository.findByUuidAndUser(uuid, user)
                .orElseThrow(() -> {
                    log.warn("Address selection failed: address not found, user uuid={}, address uuid={}", userUuid, uuid);
                    return new ResourceNotFoundException("Address not found");
                });

        if (address.isSelected()) {
            log.debug("Address is already selected, user uuid={}, address uuid={}", userUuid, uuid);
            return addressMapper.toResponse(address);
        }

        addressRepository.clearSelection(user, address);
        log.debug("Removed selection from previous address, user uuid={}", userUuid);
        address.setSelected(true);

        log.info("User selected address, user uuid={}, address uuid={}", userUuid, uuid);
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public void delete(UserPrincipal principal, UUID uuid) {
        UUID userUuid = principal.getUuid();

        User user = userRepository.findLockedByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("Address deletion failed: user not found, uuid={}", userUuid);
                    return new ResourceNotFoundException("User not found");
                });
        if (!user.isActive()) {
            log.warn("Address deletion failed: user is deleted or not verified, uuid={}", userUuid);
            throw new ResourceNotFoundException("User not found");
        }
        Address address = addressRepository.findByUuidAndUser(uuid, user)
                .orElseThrow(() -> {
                    log.warn("Address deletion failed: address not found, user uuid={}, address uuid={}", userUuid, uuid);
                    return new ResourceNotFoundException("Address not found");
                });

        addressRepository.delete(address);
        addressRepository.flush();
        log.debug("Deleted address from database, address uuid={}", uuid);

        if (address.isSelected()) {
            addressRepository.findFirstByUserOrderByCreatedAtDesc(user)
                    .ifPresentOrElse(
                            next -> {
                                next.setSelected(true);
                                log.debug("Moved selection to newest address, address uuid={}", next.getUuid());
                            },
                            () -> log.debug("No addresses left to select, user uuid={}", userUuid)
                    );
        }

        log.info("User deleted address, user uuid={}, address uuid={}", userUuid, uuid);
    }
}
