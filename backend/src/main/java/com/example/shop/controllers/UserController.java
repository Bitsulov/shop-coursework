package com.example.shop.controllers;

import com.example.shop.dtos.user.DeleteAccountRequest;
import com.example.shop.dtos.user.UserDetailResponse;
import com.example.shop.dtos.user.UserResponse;
import com.example.shop.dtos.user.UserUpdateDetails;
import com.example.shop.security.UserPrincipal;
import com.example.shop.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Публичный профиль, изменение и удаление своего аккаунта")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Публичный профиль",
            description = "Возвращает публичные данные пользователя по UUID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные пользователя получены"),
                    @ApiResponse(responseCode = "400", description = "Неверный формат UUID"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping("/{uuid}")
    public ResponseEntity<UserResponse> getByUuid(
            @Parameter(description = "UUID пользователя", required = true)
            @PathVariable UUID uuid
    ) {
        UserResponse response = userService.getByUuid(uuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Изменение профиля",
            description = "Меняет имя и телефон текущего пользователя, загружает новый аватар или удаляет старый.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Профиль изменён"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации, неверное изображение или одновременно новый аватар и его удаление"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "413", description = "Файл слишком большой"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDetailResponse> update(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Новые данные пользователя", required = true)
            @Valid @RequestPart("details") UserUpdateDetails details,
            @Parameter(description = "Новый аватар")
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        UserDetailResponse response = userService.updateDetails(principal, details, avatar);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Удаление аккаунта",
            description = "Удаляет аккаунт текущего пользователя после проверки пароля.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Аккаунт удалён"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "403", description = "Неверный пароль"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Текущий пароль", required = true)
            @Valid @RequestBody DeleteAccountRequest request
    ) {
        userService.delete(principal, request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
