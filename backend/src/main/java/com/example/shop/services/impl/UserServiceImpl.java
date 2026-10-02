package com.example.shop.services.impl;

import com.example.shop.dtos.user.DeleteAccountRequest;
import com.example.shop.dtos.user.UserDetailResponse;
import com.example.shop.dtos.user.UserResponse;
import com.example.shop.dtos.user.UserUpdateDetails;
import com.example.shop.exceptions.ForbiddenException;
import com.example.shop.exceptions.ResourceNotFoundException;
import com.example.shop.mappers.UserMapper;
import com.example.shop.models.entities.User;
import com.example.shop.repositories.AddressRepository;
import com.example.shop.repositories.UserRepository;
import com.example.shop.repositories.VerificationCodeRepository;
import com.example.shop.security.UserPrincipal;
import com.example.shop.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final UserMapper userMapper;
    private final ImageUploaderService imageUploaderService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getDetails(UserPrincipal principal) {
        UUID uuid = principal.getUuid();

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Getting principal profile failed: user not found, uuid={}", uuid);
                    return new ResourceNotFoundException("User not found");
                });

        return userMapper.toDetailResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByUuid(UUID uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Getting public profile failed: user not found, uuid={}", uuid);
                    return new ResourceNotFoundException("User not found");
                });
        if (!user.isActive()) {
            log.warn("Getting public profile failed: user is not verified or deleted, uuid={}", uuid);
            throw new ResourceNotFoundException("User not found");
        }

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserDetailResponse updateDetails(UserPrincipal principal, UserUpdateDetails details, MultipartFile avatar) {
        UUID uuid = principal.getUuid();

        boolean hasAvatar = avatar != null && !avatar.isEmpty();
        if (hasAvatar && details.isDeleteAvatar()) {
            log.warn("Profile update warn: new avatar sent together with avatar deletion, uuid={}", uuid);
            throw new IllegalArgumentException("Cannot upload and delete avatar at the same time");
        }

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Profile update failed: user not found, uuid={}", uuid);
                    return new ResourceNotFoundException("User not found");
                });
        String oldAvatarUrl = user.getAvatarUrl();

        userMapper.updateEntity(details, user);
        user.setUpdatedAt(Instant.now());
        userRepository.saveAndFlush(user);

        if (hasAvatar) {
            user.setAvatarUrl(imageUploaderService.uploadImage(avatar));
        } else if (details.isDeleteAvatar()) {
            user.setAvatarUrl(null);
        }

        if (oldAvatarUrl != null && (hasAvatar || details.isDeleteAvatar())) {
            try {
                imageUploaderService.deleteImage(oldAvatarUrl);
            } catch (RuntimeException ex) {
                log.error("Profile update error: old avatar was not deleted from storage, url={}, uuid={}", oldAvatarUrl, uuid, ex);
            }
        }

        log.info("User updated profile, uuid={}", uuid);
        return userMapper.toDetailResponse(user);
    }

    @Override
    @Transactional
    public void delete(UserPrincipal principal, DeleteAccountRequest request) {
        UUID uuid = principal.getUuid();

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Account deletion failed: user not found, uuid={}", uuid);
                    return new ResourceNotFoundException("User not found");
                });
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Account deletion warn: invalid password, uuid={}", uuid);
            throw new ForbiddenException("Invalid password");
        }

        addressRepository.deleteAllByUser(user);
        verificationCodeRepository.deleteAllByUser(user);

        String avatarUrl = user.getAvatarUrl();
        Instant now = Instant.now();

        user.setEmail(null);
        user.setName(null);
        user.setPhone(null);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setAvatarUrl(null);
        user.setActive(false);
        user.setDeletedAt(now);
        user.setUpdatedAt(now);
        userRepository.saveAndFlush(user);

        if (avatarUrl != null) {
            try {
                imageUploaderService.deleteImage(avatarUrl);
            } catch (RuntimeException ex) {
                log.error("Account deletion error: avatar was not deleted from storage, url={}, uuid={}", avatarUrl, uuid, ex);
            }
        }

        log.info("User deleted account, uuid={}", uuid);
    }
}
