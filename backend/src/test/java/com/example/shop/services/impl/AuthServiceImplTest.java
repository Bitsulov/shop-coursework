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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final Duration CODE_EXPIRATION_TIME = Duration.ofMinutes(15);
    private static final int CODE_MAX_ATTEMPTS = 5;
    private static final String EMAIL = "ivan@mail.ru";
    private static final String PASSWORD = "StrongPass123";
    private static final String CODE = "483920";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final long ACCESS_VALIDITY = 900_000L;
    private static final long REFRESH_VALIDITY = 2_592_000_000L;

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationCodeRepository verificationCodeRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailMessageProducer emailMessageProducer;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "codeExpirationTime", CODE_EXPIRATION_TIME);
        ReflectionTestUtils.setField(authService, "allowNewCodeTime", Duration.ofMinutes(2));
        ReflectionTestUtils.setField(authService, "codeMaxAttempts", CODE_MAX_ATTEMPTS);
    }

    @Nested
    @DisplayName("Регистрация")
    class Register {

        @Test
        @DisplayName("Регистрация с несовпадающими паролями отклоняется, и пользователь не сохраняется")
        void passwordsMismatch() {
            SignUpRequest request = signUpRequest(EMAIL, PASSWORD, "OtherPass123");

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Passwords do not match");
            verifyNoInteractions(userRepository, emailMessageProducer);
        }

        @Test
        @DisplayName("Зарегистрироваться на email подтверждённого пользователя нельзя")
        void activeUserExists() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(true, false)));

            assertThatThrownBy(() -> authService.register(signUpRequest(EMAIL, PASSWORD, PASSWORD)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("User already exists");
            verify(userRepository, never()).save(any());
            verifyNoInteractions(emailMessageProducer);
        }

        @Test
        @DisplayName("Без роли USER в базе регистрация прерывается, и пользователь не сохраняется")
        void noDefaultRole() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.register(signUpRequest(EMAIL, PASSWORD, PASSWORD)))
                    .isInstanceOf(IllegalStateException.class);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Новый пользователь сохраняется неактивным, код подтверждения хешируется и отправляется в очередь писем")
        void newUser() {
            Role role = role(RoleEnum.USER);
            SignUpRequest request = signUpRequest("Ivan@Mail.ru", PASSWORD, PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.of(role));
            when(userMapper.toEntity(request)).thenReturn(new User());
            when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "hash:" + invocation.getArgument(0));
            when(verificationCodeRepository.findByUserAndType(any(), eq(VerificationCodesType.REGISTER)))
                    .thenReturn(Optional.empty());
            Instant before = Instant.now();

            authService.register(request);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertThat(saved.getEmail()).isEqualTo(EMAIL);
            assertThat(saved.getUuid()).isNotNull();
            assertThat(saved.getRole()).isSameAs(role);
            assertThat(saved.getPassword()).isEqualTo("hash:" + PASSWORD);
            assertThat(saved.isActive()).isFalse();

            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailMessageProducer).sendRegisterVerificationEmail(eq(EMAIL), codeCaptor.capture());
            String sentCode = codeCaptor.getValue();
            assertThat(sentCode).matches("\\d{6}");

            ArgumentCaptor<VerificationCode> storedCaptor = ArgumentCaptor.forClass(VerificationCode.class);
            verify(verificationCodeRepository).save(storedCaptor.capture());
            VerificationCode stored = storedCaptor.getValue();
            assertThat(stored.getCode()).isEqualTo("hash:" + sentCode);
            assertThat(stored.getUser()).isSameAs(saved);
            assertThat(stored.getType()).isEqualTo(VerificationCodesType.REGISTER);
            assertThat(stored.getAttempts()).isZero();
            assertThat(stored.getCodeExpiresAt())
                    .isBetween(before.plus(CODE_EXPIRATION_TIME), Instant.now().plus(CODE_EXPIRATION_TIME));
        }

        @Test
        @DisplayName("Неподтверждённый пользователь может зарегистрироваться снова, при этом обновляется его старая запись")
        void inactiveUserExists() {
            User existing = user(false, false);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
            when(passwordEncoder.encode(anyString())).thenReturn("hash");
            when(verificationCodeRepository.findByUserAndType(existing, VerificationCodesType.REGISTER))
                    .thenReturn(Optional.empty());

            authService.register(signUpRequest(EMAIL, PASSWORD, PASSWORD));

            verify(userRepository).save(existing);
            verify(passwordEncoder).encode(PASSWORD);
            verifyNoInteractions(roleRepository, userMapper);
            verify(emailMessageProducer).sendRegisterVerificationEmail(eq(EMAIL), anyString());
        }

        @Test
        @DisplayName("Новый код нельзя запросить раньше чем через 2 минуты после предыдущего")
        void tooEarly() {
            User existing = user(false, false);
            VerificationCode oldCode = codeSentAgo(existing, Duration.ofMinutes(1));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
            when(passwordEncoder.encode(anyString())).thenReturn("hash");
            when(verificationCodeRepository.findByUserAndType(existing, VerificationCodesType.REGISTER))
                    .thenReturn(Optional.of(oldCode));

            assertThatThrownBy(() -> authService.register(signUpRequest(EMAIL, PASSWORD, PASSWORD)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Please wait before requesting a new verification code");
            verify(verificationCodeRepository, never()).delete(any());
            verifyNoInteractions(emailMessageProducer);
        }

        @Test
        @DisplayName("Через 2 минуты старый код удаляется из базы, а новый отправляется в очередь писем")
        void afterWait() {
            User existing = user(false, false);
            VerificationCode oldCode = codeSentAgo(existing, Duration.ofMinutes(3));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
            when(passwordEncoder.encode(anyString())).thenReturn("hash");
            when(verificationCodeRepository.findByUserAndType(existing, VerificationCodesType.REGISTER))
                    .thenReturn(Optional.of(oldCode));

            authService.register(signUpRequest(EMAIL, PASSWORD, PASSWORD));

            verify(verificationCodeRepository).delete(oldCode);
            verify(verificationCodeRepository).flush();
            verify(verificationCodeRepository).save(any(VerificationCode.class));
            verify(emailMessageProducer).sendRegisterVerificationEmail(eq(EMAIL), anyString());
        }
    }

    @Nested
    @DisplayName("Повторная отправка кода")
    class Send {

        @Test
        @DisplayName("Код не отправляется на email, которого нет в базе")
        void userNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.send(sendCodeRequest("IVAN@mail.ru")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("Подтверждённому пользователю код повторно не отправляется")
        void alreadyVerified() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(true, false)));

            assertThatThrownBy(() -> authService.send(sendCodeRequest(EMAIL)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("User already verified");
            verifyNoInteractions(emailMessageProducer);
        }

        @Test
        @DisplayName("Неподтверждённый пользователь получает новый код в очередь писем")
        void unverifiedUser() {
            User existing = user(false, false);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
            when(passwordEncoder.encode(anyString())).thenReturn("hash");
            when(verificationCodeRepository.findByUserAndType(existing, VerificationCodesType.REGISTER))
                    .thenReturn(Optional.empty());

            authService.send(sendCodeRequest(EMAIL));

            verify(verificationCodeRepository).save(any(VerificationCode.class));
            verify(emailMessageProducer).sendRegisterVerificationEmail(eq(EMAIL), anyString());
        }
    }

    @Nested
    @DisplayName("Подтверждение почты")
    class Verify {

        @Test
        @DisplayName("Нельзя подтвердить почту, которой нет в базе")
        void userNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.verify(verifyRequest(EMAIL, CODE)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("Уже подтверждённую почту нельзя подтвердить повторно")
        void alreadyVerified() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(true, false)));

            assertThatThrownBy(() -> authService.verify(verifyRequest(EMAIL, CODE)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("User already verified");
        }

        @Test
        @DisplayName("Заблокированный пользователь не может подтвердить почту")
        void userBlocked() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(false, true)));

            assertThatThrownBy(() -> authService.verify(verifyRequest(EMAIL, CODE)))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("User is blocked");
            verifyNoInteractions(verificationCodeRepository);
        }

        @Test
        @DisplayName("Без кода в базе подтверждение не проходит")
        void noCode() {
            User existing = user(false, false);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
            when(verificationCodeRepository.findByUserAndType(existing, VerificationCodesType.REGISTER))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.verify(verifyRequest(EMAIL, CODE)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Verification code not found");
        }

        @Test
        @DisplayName("Код старше 15 минут не принимается, и попытка не засчитывается")
        void codeExpired() {
            User existing = user(false, false);
            VerificationCode expired = codeSentAgo(existing, CODE_EXPIRATION_TIME.plusSeconds(1));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
            when(verificationCodeRepository.findByUserAndType(existing, VerificationCodesType.REGISTER))
                    .thenReturn(Optional.of(expired));

            assertThatThrownBy(() -> authService.verify(verifyRequest(EMAIL, CODE)))
                    .isInstanceOf(GoneException.class)
                    .hasMessage("Verification code expired");
            verify(verificationCodeRepository, never()).incrementAttempts(any(), anyInt());
        }

        @Test
        @DisplayName("После 5 неудачных попыток код больше не проверяется")
        void noAttemptsLeft() {
            User existing = user(false, false);
            VerificationCode code = codeSentAgo(existing, Duration.ofMinutes(1));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
            when(verificationCodeRepository.findByUserAndType(existing, VerificationCodesType.REGISTER))
                    .thenReturn(Optional.of(code));
            when(verificationCodeRepository.incrementAttempts(code.getId(), CODE_MAX_ATTEMPTS)).thenReturn(0);

            assertThatThrownBy(() -> authService.verify(verifyRequest(EMAIL, CODE)))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("Too many verification attempts");
            verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("Неверный код не активирует пользователя, и код остаётся в базе")
        void wrongCode() {
            User existing = user(false, false);
            VerificationCode code = codeSentAgo(existing, Duration.ofMinutes(1));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
            when(verificationCodeRepository.findByUserAndType(existing, VerificationCodesType.REGISTER))
                    .thenReturn(Optional.of(code));
            when(verificationCodeRepository.incrementAttempts(code.getId(), CODE_MAX_ATTEMPTS)).thenReturn(1);
            when(passwordEncoder.matches(CODE, code.getCode())).thenReturn(false);

            assertThatThrownBy(() -> authService.verify(verifyRequest(EMAIL, CODE)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid verification code");
            assertThat(existing.isActive()).isFalse();
            verify(verificationCodeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Верный код активирует пользователя, удаляет код из базы и возвращает токены")
        void validCode() {
            User existing = user(false, false);
            VerificationCode code = codeSentAgo(existing, Duration.ofMinutes(1));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
            when(verificationCodeRepository.findByUserAndType(existing, VerificationCodesType.REGISTER))
                    .thenReturn(Optional.of(code));
            when(verificationCodeRepository.incrementAttempts(code.getId(), CODE_MAX_ATTEMPTS)).thenReturn(1);
            when(passwordEncoder.matches(CODE, code.getCode())).thenReturn(true);
            stubTokens(existing.getUuid());

            AuthResponse response = authService.verify(verifyRequest("Ivan@Mail.RU", CODE));

            assertThat(existing.isActive()).isTrue();
            verify(verificationCodeRepository).delete(code);
            assertTokens(response, existing.getUuid());
        }
    }

    @Nested
    @DisplayName("Вход")
    class Login {

        @Test
        @DisplayName("Вход с незарегистрированным email отклоняется без проверки пароля")
        void unknownEmail() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(signInRequest(EMAIL, PASSWORD)))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid email or password");
            verifyNoInteractions(authenticationManager, tokenProvider);
        }

        @ParameterizedTest
        @MethodSource("com.example.shop.services.impl.AuthServiceImplTest#authFailures")
        @DisplayName("Неверный пароль, неподтверждённая почта и блокировка дают одну и ту же ошибку входа")
        void authFails(AuthenticationException failure) {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(true, false)));
            when(authenticationManager.authenticate(any())).thenThrow(failure);

            assertThatThrownBy(() -> authService.login(signInRequest(EMAIL, "WrongPass123")))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid email or password");
            verifyNoInteractions(tokenProvider);
        }

        @Test
        @DisplayName("Пользователь с верным паролем получает токены")
        void validCredentials() {
            User existing = user(true, false);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
            stubTokens(existing.getUuid());

            AuthResponse response = authService.login(signInRequest("IVAN@MAIL.RU", PASSWORD));

            ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager).authenticate(tokenCaptor.capture());
            assertThat(tokenCaptor.getValue().getPrincipal()).isEqualTo(existing.getUuid().toString());
            assertThat(tokenCaptor.getValue().getCredentials()).isEqualTo(PASSWORD);
            assertTokens(response, existing.getUuid());
        }
    }

    @Nested
    @DisplayName("Обновление токенов")
    class Refresh {

        @Test
        @DisplayName("Поддельный или истёкший токен не обновляется")
        void invalidToken() {
            when(tokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(false);

            assertThatThrownBy(() -> authService.refresh(refreshRequest()))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid refresh token");
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Access-токен нельзя использовать вместо refresh-токена")
        void accessToken() {
            when(tokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
            when(tokenProvider.getTokenType(REFRESH_TOKEN)).thenReturn("access");

            assertThatThrownBy(() -> authService.refresh(refreshRequest()))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Provided token is not a refresh token");
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Токен удалённого пользователя не обновляется")
        void userMissing() {
            UUID uuid = UUID.randomUUID();
            stubValidRefreshToken(uuid);
            when(userRepository.findByUuid(uuid)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refresh(refreshRequest()))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid refresh token");
        }

        @Test
        @DisplayName("Удалённому пользователю новые токены не выдаются, возвращается ошибка недействительного токена")
        void userDeleted() {
            User existing = user(false, false);
            existing.setDeletedAt(Instant.now());
            stubValidRefreshToken(existing.getUuid());
            when(userRepository.findByUuid(existing.getUuid())).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> authService.refresh(refreshRequest()))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid refresh token");
        }

        @Test
        @DisplayName("Пользователь с неподтверждённой почтой не получает новые токены")
        void notVerified() {
            User existing = user(false, false);
            stubValidRefreshToken(existing.getUuid());
            when(userRepository.findByUuid(existing.getUuid())).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> authService.refresh(refreshRequest()))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("Email is not verified");
        }

        @Test
        @DisplayName("Заблокированный пользователь не получает новые токены")
        void userBlocked() {
            User existing = user(true, true);
            stubValidRefreshToken(existing.getUuid());
            when(userRepository.findByUuid(existing.getUuid())).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> authService.refresh(refreshRequest()))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("User is blocked");
        }

        @Test
        @DisplayName("Действующий refresh-токен меняется на новую пару токенов")
        void validToken() {
            User existing = user(true, false);
            stubValidRefreshToken(existing.getUuid());
            when(userRepository.findByUuid(existing.getUuid())).thenReturn(Optional.of(existing));
            stubTokens(existing.getUuid());

            AuthResponse response = authService.refresh(refreshRequest());

            assertTokens(response, existing.getUuid());
        }

        private void stubValidRefreshToken(UUID uuid) {
            when(tokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
            when(tokenProvider.getTokenType(REFRESH_TOKEN)).thenReturn("refresh");
            when(tokenProvider.getUsernameFromJWT(REFRESH_TOKEN)).thenReturn(uuid.toString());
        }
    }

    static Stream<Arguments> authFailures() {
        return Stream.of(
                argumentSet("Неверный пароль", new BadCredentialsException("Bad credentials")),
                argumentSet("Почта не подтверждена", new DisabledException("User is disabled")),
                argumentSet("Пользователь заблокирован", new LockedException("User account is locked"))
        );
    }

    private void stubTokens(UUID uuid) {
        when(tokenProvider.generateAccessToken(uuid)).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(uuid)).thenReturn("refresh-token");
        when(tokenProvider.getAccessTokenValidityInMillis()).thenReturn(ACCESS_VALIDITY);
        when(tokenProvider.getRefreshTokenValidityInMillis()).thenReturn(REFRESH_VALIDITY);
    }

    private void assertTokens(AuthResponse response, UUID uuid) {
        assertThat(response.getUuid()).isEqualTo(uuid);
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getAccessTokenExpiresIn()).isEqualTo(ACCESS_VALIDITY);
        assertThat(response.getRefreshTokenExpiresIn()).isEqualTo(REFRESH_VALIDITY);
    }

    private static User user(boolean active, boolean blocked) {
        User user = new User();
        user.setId(1L);
        user.setUuid(UUID.randomUUID());
        user.setEmail(EMAIL);
        user.setPassword("hash");
        user.setName("Иван");
        user.setActive(active);
        user.setBlocked(blocked);
        user.setRole(role(RoleEnum.USER));
        return user;
    }

    private static Role role(RoleEnum name) {
        Role role = new Role();
        ReflectionTestUtils.setField(role, "name", name);
        return role;
    }

    private static VerificationCode codeSentAgo(User user, Duration ago) {
        Instant expiresAt = Instant.now().minus(ago).plus(CODE_EXPIRATION_TIME);
        VerificationCode code = new VerificationCode("code-hash", expiresAt, EMAIL, user, VerificationCodesType.REGISTER, 0);
        code.setId(10L);
        return code;
    }

    private static SignUpRequest signUpRequest(String email, String password, String confirmPassword) {
        SignUpRequest request = new SignUpRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        request.setName("Иван");
        return request;
    }

    private static SendCodeRequest sendCodeRequest(String email) {
        SendCodeRequest request = new SendCodeRequest();
        request.setEmail(email);
        return request;
    }

    private static VerifyRequest verifyRequest(String email, String code) {
        VerifyRequest request = new VerifyRequest();
        request.setEmail(email);
        request.setVerificationCode(code);
        return request;
    }

    private static SignInRequest signInRequest(String email, String password) {
        SignInRequest request = new SignInRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private static RefreshRequest refreshRequest() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken(REFRESH_TOKEN);
        return request;
    }
}
