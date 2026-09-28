package com.example.shop.services.impl;

import com.example.shop.dtos.auth.AuthResponse;
import com.example.shop.dtos.auth.RefreshRequest;
import com.example.shop.dtos.auth.SendCodeRequest;
import com.example.shop.dtos.auth.SignInRequest;
import com.example.shop.dtos.auth.SignUpRequest;
import com.example.shop.dtos.auth.VerifyRequest;
import com.example.shop.exceptions.ConflictException;
import com.example.shop.exceptions.ForbiddenException;
import com.example.shop.exceptions.GoneException;
import com.example.shop.exceptions.ResourceNotFoundException;
import com.example.shop.exceptions.UnauthorizedException;
import com.example.shop.mappers.UserMapper;
import com.example.shop.models.entities.Role;
import com.example.shop.models.entities.User;
import com.example.shop.models.entities.VerificationCode;
import com.example.shop.models.enums.RoleEnum;
import com.example.shop.models.enums.VerificationCodesType;
import com.example.shop.repositories.RoleRepository;
import com.example.shop.repositories.UserRepository;
import com.example.shop.repositories.VerificationCodeRepository;
import com.example.shop.security.JwtTokenProvider;
import com.example.shop.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom secureRandom = new SecureRandom();

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailMessageProducer emailMessageProducer;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.code-expiration-time}")
    private Duration codeExpirationTime;

    @Value("${app.allow-new-code-time}")
    private Duration allowNewCodeTime;

    @Value("${app.code-max-attempts}")
    private int codeMaxAttempts;

    @Override
    @Transactional
    public void register(SignUpRequest signUpRequest) {
        String email = toLowerCase(signUpRequest.getEmail());
        log.info("START register, email={}", email);

        if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
            log.warn("Registration failed: passwords do not match, email={}", email);
            throw new IllegalArgumentException("Passwords do not match");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && existingUser.get().isActive()) {
            log.warn("Registration failed: user already exists, email={}", email);
            throw new ConflictException("User already exists");
        }

        User user = existingUser.orElseGet(() -> {
            Role role = roleRepository.findByName(RoleEnum.USER)
                    .orElseThrow(() -> {
                        log.error("Registration failed: default role is missing, role={}", RoleEnum.USER);
                        return new IllegalStateException("Default role not found");
                    });

            User newUser = userMapper.toEntity(signUpRequest);
            newUser.setUuid(UUID.randomUUID());
            newUser.setEmail(email);
            newUser.setRole(role);
            newUser.setCreatedAt(Instant.now());
            log.debug("Created new user, uuid={}, role={}, email={}", newUser.getUuid(), role.getName(), email);
            return newUser;
        });

        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setActive(false);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        log.debug("Saved unverified user, uuid={}, email={}", user.getUuid(), email);

        createAndSendVerificationCode(user, email);
        log.info("END register: user saved and code sent, email={}", email);
    }

    @Override
    @Transactional
    public void send(SendCodeRequest sendCodeRequest) {
        String email = toLowerCase(sendCodeRequest.getEmail());
        log.info("START send, email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Sending code failed: user not found, email={}", email);
                    return new ResourceNotFoundException("User not found");
                });
        if (user.isActive()) {
            log.warn("Sending code skipped: user already verified, email={}", email);
            throw new ConflictException("User already verified");
        }
        log.debug("Resending verification code, uuid={}, email={}", user.getUuid(), email);

        createAndSendVerificationCode(user, email);
        log.info("END send: code resent, email={}", email);
    }

    @Override
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public AuthResponse verify(VerifyRequest verifyRequest) {
        String email = toLowerCase(verifyRequest.getEmail());
        log.info("START verify, email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Verification failed: user not found, email={}", email);
                    return new ResourceNotFoundException("User not found");
                });
        if (user.isActive()) {
            log.warn("Verification skipped: user already verified, email={}", email);
            throw new ConflictException("User already verified");
        }
        if (user.isBlocked()) {
            log.warn("Verification failed: user is blocked, email={}", email);
            throw new ForbiddenException("User is blocked");
        }

        VerificationCode verificationCode = verificationCodeRepository
                .findByUserAndType(user, VerificationCodesType.REGISTER)
                .orElseThrow(() -> {
                    log.warn("Verification failed: code not found, email={}", email);
                    return new ResourceNotFoundException("Verification code not found");
                });

        if (verificationCode.getCodeExpiresAt().isBefore(Instant.now())) {
            log.warn("Verification failed: code expired, expiresAt={}, email={}", verificationCode.getCodeExpiresAt(), email);
            throw new GoneException("Verification code expired");
        }
        if (verificationCodeRepository.incrementAttempts(verificationCode.getId(), codeMaxAttempts) == 0) {
            log.warn("Verification failed: all attempts spent, maxAttempts={}, email={}", codeMaxAttempts, email);
            throw new ForbiddenException("Too many verification attempts");
        }
        if (!passwordEncoder.matches(verifyRequest.getVerificationCode(), verificationCode.getCode())) {
            log.warn("Verification failed: invalid code, email={}", email);
            throw new IllegalArgumentException("Invalid verification code");
        }

        user.setActive(true);
        user.setUpdatedAt(Instant.now());
        verificationCodeRepository.delete(verificationCode);
        log.debug("Enabled account after verification, uuid={}, email={}", user.getUuid(), email);

        AuthResponse authResponse = buildAuthResponse(user);
        log.info("END verify: account enabled and tokens issued, email={}", email);
        return authResponse;
    }

    @Override
    public AuthResponse login(SignInRequest signInRequest) {
        String email = toLowerCase(signInRequest.getEmail());
        log.info("START login, email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found, email={}", email);
                    return new UnauthorizedException("Invalid email or password");
                });
        log.debug("Authenticating user, uuid={}, active={}, blocked={}",
                user.getUuid(), user.isActive(), user.isBlocked());

        String uuid = user.getUuid().toString();
        String password = signInRequest.getPassword();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(uuid, password);

        try {
            authenticationManager.authenticate(authToken);
        } catch (AuthenticationException ex) {
            log.warn("Login failed: {}, email={}", ex.getMessage(), email);
            throw new UnauthorizedException("Invalid email or password");
        }
        log.debug("Authenticated user, uuid={}, email={}", uuid, email);

        AuthResponse authResponse = buildAuthResponse(user);
        log.info("END login: tokens issued, email={}", email);
        return authResponse;
    }

    @Override
    public AuthResponse refresh(RefreshRequest refreshRequest) {
        log.info("START refresh");
        String refreshToken = refreshRequest.getRefreshToken();

        if (!tokenProvider.validateToken(refreshToken)) {
            log.warn("Refresh failed: invalid token");
            throw new UnauthorizedException("Invalid refresh token");
        }
        if (!"refresh".equals(tokenProvider.getTokenType(refreshToken))) {
            log.warn("Refresh failed: token is not a refresh token");
            throw new UnauthorizedException("Provided token is not a refresh token");
        }

        UUID uuid = UUID.fromString(tokenProvider.getUsernameFromJWT(refreshToken));
        log.debug("Refreshing tokens, uuid={}", uuid);

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Refresh failed: user from a valid token is missing, uuid={}", uuid);
                    return new UnauthorizedException("Invalid refresh token");
                });

        if (!user.isActive()) {
            log.warn("Refresh failed: user is not verified, uuid={}", uuid);
            throw new ForbiddenException("Email is not verified");
        }
        if (user.isBlocked()) {
            log.warn("Refresh failed: user is blocked, uuid={}", uuid);
            throw new ForbiddenException("User is blocked");
        }

        AuthResponse authResponse = buildAuthResponse(user);
        log.info("END refresh: tokens issued, uuid={}", uuid);
        return authResponse;
    }

    private void createAndSendVerificationCode(User user, String email) {
        verificationCodeRepository.findByUserAndType(user, VerificationCodesType.REGISTER)
                .ifPresent(oldCode -> {
                    Instant sentAt = oldCode.getCodeExpiresAt().minus(codeExpirationTime);
                    if (Instant.now().isBefore(sentAt.plus(allowNewCodeTime))) {
                        log.warn("Sending code rejected: requested too early, sentAt={}, email={}", sentAt, email);
                        throw new IllegalArgumentException("Please wait before requesting a new verification code");
                    }
                    verificationCodeRepository.delete(oldCode);
                    verificationCodeRepository.flush();
                    log.debug("Deleted previous verification code, sentAt={}, email={}", sentAt, email);
                });

        String code = generateSixDigitCode();
        Instant expiresAt = Instant.now().plus(codeExpirationTime);
        VerificationCode verificationCode = new VerificationCode(passwordEncoder.encode(code), expiresAt, email, user, VerificationCodesType.REGISTER, 0);

        log.debug("Generated verification code, code={}, expiresAt={}, email={}", code, expiresAt, email);

        verificationCodeRepository.save(verificationCode);
        emailMessageProducer.sendRegisterVerificationEmail(email, code);
        log.info("Created and sent verification code, email={}", email);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getUuid());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUuid());
        long accessExpiresIn = tokenProvider.getAccessTokenValidityInMillis();
        long refreshExpiresIn = tokenProvider.getRefreshTokenValidityInMillis();
        log.info("Issued tokens, accessExpiresIn={}ms, refreshExpiresIn={}ms, uuid={}",
                accessExpiresIn, refreshExpiresIn, user.getUuid());

        return new AuthResponse(user.getUuid(), accessToken, refreshToken, accessExpiresIn, refreshExpiresIn);
    }

    private String toLowerCase(String email) {
        return email.toLowerCase(Locale.ROOT);
    }

    private String generateSixDigitCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}