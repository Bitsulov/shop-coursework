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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String PASSWORD = "StrongPass123";
    private static final String OLD_AVATAR = "http://localhost:9000/shop-files/client_old.jpg";
    private static final String NEW_AVATAR = "http://localhost:9000/shop-files/client_new.jpg";

    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private VerificationCodeRepository verificationCodeRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ImageUploaderService imageUploaderService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("Получение своего профиля")
    class GetDetails {

        @Test
        @DisplayName("Данные пользователя загружаются из базы данных по uuid из токена и возвращаются в полном объёме")
        void found() {
            User user = user(null);
            UserDetailResponse expected = new UserDetailResponse();
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(userMapper.toDetailResponse(user)).thenReturn(expected);

            UserDetailResponse response = userService.getDetails(new UserPrincipal(user));

            assertThat(response).isSameAs(expected);
        }

        @Test
        @DisplayName("При отсутствии пользователя из токена в базе данных возвращается ошибка «не найден»")
        void notFound() {
            User user = user(null);
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getDetails(new UserPrincipal(user)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("Публичный профиль")
    class GetByUuid {

        @Test
        @DisplayName("Для подтверждённого пользователя возвращаются публичные данные")
        void activeUser() {
            User user = user(null);
            UserResponse expected = new UserResponse();
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(expected);

            assertThat(userService.getByUuid(user.getUuid())).isSameAs(expected);
        }

        @Test
        @DisplayName("Для неподтверждённого или удалённого пользователя возвращается ошибка «не найден»")
        void inactiveUser() {
            User user = user(null);
            user.setActive(false);
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.getByUuid(user.getUuid()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
            verify(userMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("Для несуществующего uuid возвращается ошибка «не найден»")
        void notFound() {
            UUID uuid = UUID.randomUUID();
            when(userRepository.findByUuid(uuid)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getByUuid(uuid))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("Изменение профиля")
    class UpdateDetails {

        @Test
        @DisplayName("Запрос с новым аватаром и одновременным удалением аватара отклоняется без изменений в базе данных и хранилище")
        void avatarAndDeletion() {
            User user = user(OLD_AVATAR);

            assertThatThrownBy(() -> userService.updateDetails(new UserPrincipal(user), details(true), avatarFile()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cannot upload and delete avatar at the same time");
            verifyNoInteractions(userRepository, imageUploaderService);
        }

        @Test
        @DisplayName("Для удалённого пользователя возвращается ошибка «не найден», профиль в базе данных не изменяется")
        void inactiveUser() {
            User user = user(OLD_AVATAR);
            user.setActive(false);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.updateDetails(new UserPrincipal(user), details(false), avatarFile()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
            verify(userRepository, never()).saveAndFlush(any());
            verifyNoInteractions(imageUploaderService);
        }

        @Test
        @DisplayName("Имя и телефон сохраняются в базе данных без обращения к хранилищу файлов")
        void onlyFields() {
            User user = user(OLD_AVATAR);
            UserUpdateDetails details = details(false);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));

            userService.updateDetails(new UserPrincipal(user), details, null);

            verify(userMapper).updateEntity(details, user);
            verify(userRepository).saveAndFlush(user);
            assertThat(user.getAvatarUrl()).isEqualTo(OLD_AVATAR);
            assertThat(user.getUpdatedAt()).isNotNull();
            verifyNoInteractions(imageUploaderService);
        }

        @Test
        @DisplayName("Новый аватар загружается в хранилище после сохранения изменений в базе данных, старый файл удаляется последним")
        void newAvatar() {
            User user = user(OLD_AVATAR);
            MockMultipartFile avatar = avatarFile();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(imageUploaderService.uploadImage(avatar)).thenReturn(NEW_AVATAR);

            userService.updateDetails(new UserPrincipal(user), details(false), avatar);

            assertThat(user.getAvatarUrl()).isEqualTo(NEW_AVATAR);
            InOrder order = inOrder(userRepository, imageUploaderService);
            order.verify(userRepository).saveAndFlush(user);
            order.verify(imageUploaderService).uploadImage(avatar);
            order.verify(imageUploaderService).deleteImage(OLD_AVATAR);
        }

        @Test
        @DisplayName("Первый аватар загружается в хранилище без удаления файлов")
        void firstAvatar() {
            User user = user(null);
            MockMultipartFile avatar = avatarFile();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(imageUploaderService.uploadImage(avatar)).thenReturn(NEW_AVATAR);

            userService.updateDetails(new UserPrincipal(user), details(false), avatar);

            assertThat(user.getAvatarUrl()).isEqualTo(NEW_AVATAR);
            verify(imageUploaderService, never()).deleteImage(anyString());
        }

        @Test
        @DisplayName("При удалении аватара ссылка в базе данных обнуляется, файл удаляется из хранилища")
        void deleteAvatar() {
            User user = user(OLD_AVATAR);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));

            userService.updateDetails(new UserPrincipal(user), details(true), null);

            assertThat(user.getAvatarUrl()).isNull();
            verify(imageUploaderService).deleteImage(OLD_AVATAR);
            verify(imageUploaderService, never()).uploadImage(any());
        }

        @Test
        @DisplayName("При ошибке загрузки в хранилище старый аватар сохраняется в базе данных и в хранилище")
        void uploadFails() {
            User user = user(OLD_AVATAR);
            MockMultipartFile avatar = avatarFile();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(imageUploaderService.uploadImage(avatar)).thenThrow(new IllegalStateException("Image upload failed"));

            assertThatThrownBy(() -> userService.updateDetails(new UserPrincipal(user), details(false), avatar))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(user.getAvatarUrl()).isEqualTo(OLD_AVATAR);
            verify(imageUploaderService, never()).deleteImage(anyString());
        }

        @Test
        @DisplayName("Ошибка удаления старого файла из хранилища не препятствует обновлению профиля")
        void oldFileNotDeleted() {
            User user = user(OLD_AVATAR);
            MockMultipartFile avatar = avatarFile();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(imageUploaderService.uploadImage(avatar)).thenReturn(NEW_AVATAR);
            doThrow(new IllegalStateException("Image deletion failed")).when(imageUploaderService).deleteImage(OLD_AVATAR);

            userService.updateDetails(new UserPrincipal(user), details(false), avatar);

            assertThat(user.getAvatarUrl()).isEqualTo(NEW_AVATAR);
        }
    }

    @Nested
    @DisplayName("Удаление аккаунта")
    class Delete {

        @Test
        @DisplayName("При неверном пароле аккаунт не удаляется, данные в базе данных не изменяются")
        void wrongPassword() {
            User user = user(OLD_AVATAR);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, "hash")).thenReturn(false);

            assertThatThrownBy(() -> userService.delete(new UserPrincipal(user), deleteRequest()))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("Invalid password");
            assertThat(user.getDeletedAt()).isNull();
            assertThat(user.getEmail()).isEqualTo("ivan@mail.ru");
            verifyNoInteractions(addressRepository, verificationCodeRepository, imageUploaderService);
            verify(userRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Адреса и коды удаляются из базы данных, личные данные стираются, аватар удаляется из хранилища последним")
        void success() {
            User user = user(OLD_AVATAR);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, "hash")).thenReturn(true);
            when(passwordEncoder.encode(anyString())).thenReturn("random-hash");

            userService.delete(new UserPrincipal(user), deleteRequest());

            assertThat(user.getEmail()).isNull();
            assertThat(user.getName()).isNull();
            assertThat(user.getPhone()).isNull();
            assertThat(user.getAvatarUrl()).isNull();
            assertThat(user.getPassword()).isEqualTo("random-hash");
            assertThat(user.isActive()).isFalse();
            assertThat(user.getDeletedAt()).isNotNull();
            InOrder order = inOrder(addressRepository, verificationCodeRepository, userRepository, imageUploaderService);
            order.verify(addressRepository).deleteAllByUser(user);
            order.verify(verificationCodeRepository).deleteAllByUser(user);
            order.verify(userRepository).saveAndFlush(user);
            order.verify(imageUploaderService).deleteImage(OLD_AVATAR);
        }

        @Test
        @DisplayName("Ошибка удаления аватара из хранилища не препятствует удалению аккаунта")
        void fileNotDeleted() {
            User user = user(OLD_AVATAR);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, "hash")).thenReturn(true);
            when(passwordEncoder.encode(anyString())).thenReturn("random-hash");
            doThrow(new IllegalStateException("Image deletion failed")).when(imageUploaderService).deleteImage(OLD_AVATAR);

            userService.delete(new UserPrincipal(user), deleteRequest());

            assertThat(user.getDeletedAt()).isNotNull();
            verify(userRepository).saveAndFlush(user);
        }

        @Test
        @DisplayName("При отсутствии пользователя из токена в базе данных возвращается ошибка «не найден»")
        void notFound() {
            User user = user(null);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.delete(new UserPrincipal(user), deleteRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
            verifyNoInteractions(addressRepository, verificationCodeRepository, imageUploaderService);
        }
    }

    private static User user(String avatarUrl) {
        User user = new User();
        user.setId(1L);
        user.setUuid(UUID.randomUUID());
        user.setEmail("ivan@mail.ru");
        user.setPassword("hash");
        user.setName("Иван");
        user.setPhone("+79991234567");
        user.setAvatarUrl(avatarUrl);
        user.setActive(true);
        return user;
    }

    private static UserUpdateDetails details(boolean deleteAvatar) {
        UserUpdateDetails details = new UserUpdateDetails();
        details.setName("Пётр");
        details.setPhone("+79990000000");
        details.setDeleteAvatar(deleteAvatar);
        return details;
    }

    private static MockMultipartFile avatarFile() {
        return new MockMultipartFile("avatar", "avatar.png", "image/png", new byte[]{1, 2, 3});
    }

    private static DeleteAccountRequest deleteRequest() {
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword(PASSWORD);
        return request;
    }
}