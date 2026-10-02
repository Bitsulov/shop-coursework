package com.example.shop.controllers;

import com.example.shop.dtos.auth.AuthResponse;
import com.example.shop.dtos.auth.RefreshRequest;
import com.example.shop.dtos.auth.SendCodeRequest;
import com.example.shop.dtos.auth.SignInRequest;
import com.example.shop.dtos.auth.SignUpRequest;
import com.example.shop.dtos.auth.VerifyRequest;
import com.example.shop.dtos.user.UserDetailResponse;
import com.example.shop.security.UserPrincipal;
import com.example.shop.services.AuthService;
import com.example.shop.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Регистрация, подтверждение почты, вход и обновление токенов")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт пользователя и отправляет код подтверждения на адрес электронной почты.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Регистрация начата, код отправлен"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации, пароли не совпадают или код запрошен слишком рано"),
                    @ApiResponse(responseCode = "409", description = "Пользователь с таким адресом уже подтверждён"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Parameter(description = "Данные для регистрации", required = true)
            @Valid @RequestBody SignUpRequest request
    ) {
        authService.register(request);
        return new ResponseEntity<>("Registration initiated. Check your email for verification code.", HttpStatus.CREATED);
    }

    @Operation(
            summary = "Повторная отправка кода подтверждения",
            description = "Отправляет новый код подтверждения на адрес электронной почты.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Код отправлен повторно"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или код запрошен слишком рано"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "409", description = "Пользователь уже подтверждён"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/send")
    public ResponseEntity<String> send(
            @Parameter(description = "Адрес электронной почты", required = true)
            @Valid @RequestBody SendCodeRequest request
    ) {
        authService.send(request);
        return new ResponseEntity<>("Code sent. Check your email for verification code.", HttpStatus.OK);
    }

    @Operation(
            summary = "Подтверждение регистрации",
            description = "Подтверждает адрес электронной почты по коду из письма и выдаёт токены.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Регистрация подтверждена, токены выданы"),
                    @ApiResponse(responseCode = "400", description = "Неверный код подтверждения"),
                    @ApiResponse(responseCode = "403", description = "Пользователь заблокирован или исчерпал попытки"),
                    @ApiResponse(responseCode = "404", description = "Пользователь или код не найдены"),
                    @ApiResponse(responseCode = "409", description = "Пользователь уже подтверждён"),
                    @ApiResponse(responseCode = "410", description = "Код подтверждения истёк"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(
            @Parameter(description = "Адрес электронной почты и код подтверждения", required = true)
            @Valid @RequestBody VerifyRequest request
    ) {
        AuthResponse response = authService.verify(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Вход",
            description = "Проверяет учётные данные и возвращает access- и refresh-токены.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Вход выполнен, токены выданы"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                    @ApiResponse(responseCode = "401", description = "Неверный адрес электронной почты или пароль"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Данные для входа", required = true)
            @Valid @RequestBody SignInRequest request
    ) {
        AuthResponse response = authService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Обновление токенов",
            description = "Выдаёт новую пару токенов по действующему refresh-токену.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Токены обновлены"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                    @ApiResponse(responseCode = "401", description = "Refresh-токен недействителен"),
                    @ApiResponse(responseCode = "403", description = "Пользователь не подтверждён или заблокирован"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Parameter(description = "Refresh-токен", required = true)
            @Valid @RequestBody RefreshRequest request
    ) {
        AuthResponse response = authService.refresh(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Текущий пользователь",
            description = "Возвращает полные текущего авторизованного пользователя.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные пользователя получены"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping("/user")
    public ResponseEntity<UserDetailResponse> getUser(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserDetailResponse response = userService.getDetails(principal);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
