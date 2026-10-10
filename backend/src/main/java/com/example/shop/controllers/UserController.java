package com.example.shop.controllers;

import com.example.shop.dtos.address.AddressRequest;
import com.example.shop.dtos.address.AddressResponse;
import com.example.shop.dtos.user.DeleteAccountRequest;
import com.example.shop.dtos.user.UserDetailResponse;
import com.example.shop.dtos.user.UserResponse;
import com.example.shop.dtos.user.UserUpdateDetails;
import com.example.shop.security.UserPrincipal;
import com.example.shop.services.AddressService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Публичный профиль, изменение и удаление своего аккаунта, адреса доставки")
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

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

    @Operation(
            summary = "Список адресов",
            description = "Возвращает адреса доставки текущего пользователя. Выбранный адрес идёт первым, остальные от новых к старым.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Адреса получены"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>> getAddresses(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<AddressResponse> response = addressService.getAll(principal);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Адрес по UUID",
            description = "Возвращает один адрес доставки текущего пользователя.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Адрес получен"),
                    @ApiResponse(responseCode = "400", description = "Неверный формат UUID"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь или адрес не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping("/addresses/{uuid}")
    public ResponseEntity<AddressResponse> getAddress(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "UUID адреса", required = true)
            @PathVariable UUID uuid
    ) {
        AddressResponse response = addressService.getByUuid(principal, uuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Создание адреса",
            description = "Добавляет адрес доставки текущему пользователю. Первый адрес сразу становится выбранным. У пользователя может быть не больше 10 адресов.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Адрес создан"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "409", description = "Достигнут лимит адресов"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> createAddress(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Данные адреса", required = true)
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse response = addressService.create(principal, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Изменение адреса",
            description = "Заменяет данные адреса доставки текущего пользователя.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Адрес изменён"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или неверный формат UUID"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь или адрес не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PutMapping("/addresses/{uuid}")
    public ResponseEntity<AddressResponse> updateAddress(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "UUID адреса", required = true)
            @PathVariable UUID uuid,
            @Parameter(description = "Новые данные адреса", required = true)
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse response = addressService.update(principal, uuid, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Выбор адреса",
            description = "Делает адрес выбранным и снимает выбор с остальных адресов текущего пользователя.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Адрес выбран"),
                    @ApiResponse(responseCode = "400", description = "Неверный формат UUID"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь или адрес не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PatchMapping("/addresses/{uuid}/select")
    public ResponseEntity<AddressResponse> selectAddress(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "UUID адреса", required = true)
            @PathVariable UUID uuid
    ) {
        AddressResponse response = addressService.select(principal, uuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Удаление адреса",
            description = "Удаляет адрес доставки текущего пользователя. Если удалён выбранный адрес, выбранным становится самый новый из оставшихся.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Адрес удалён"),
                    @ApiResponse(responseCode = "400", description = "Неверный формат UUID"),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                    @ApiResponse(responseCode = "404", description = "Пользователь или адрес не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @DeleteMapping("/addresses/{uuid}")
    public ResponseEntity<Void> deleteAddress(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "UUID адреса", required = true)
            @PathVariable UUID uuid
    ) {
        addressService.delete(principal, uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
