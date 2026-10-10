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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Nested
    @DisplayName("Список адресов")
    class GetAll {

        @Test
        @DisplayName("Адреса пользователя из токена загружаются из базы данных и возвращаются в ответе")
        void found() {
            User user = user();
            List<Address> addresses = List.of(address(user, true), address(user, false));
            List<AddressResponse> expected = List.of(new AddressResponse(), new AddressResponse());
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findAllByUserOrderBySelectedDescCreatedAtDesc(user)).thenReturn(addresses);
            when(addressMapper.toResponses(addresses)).thenReturn(expected);

            assertThat(addressService.getAll(new UserPrincipal(user))).isSameAs(expected);
        }

        @Test
        @DisplayName("При отсутствии пользователя из токена в базе данных возвращается ошибка «не найден»")
        void userNotFound() {
            User user = user();
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.getAll(new UserPrincipal(user)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("Получение адреса")
    class GetByUuid {

        @Test
        @DisplayName("Адрес пользователя из токена возвращается в ответе")
        void found() {
            User user = user();
            Address address = address(user, true);
            AddressResponse expected = new AddressResponse();
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(address.getUuid(), user)).thenReturn(Optional.of(address));
            when(addressMapper.toResponse(address)).thenReturn(expected);

            assertThat(addressService.getByUuid(new UserPrincipal(user), address.getUuid())).isSameAs(expected);
        }

        @Test
        @DisplayName("Для чужого или несуществующего адреса возвращается ошибка «не найден»")
        void addressNotFound() {
            User user = user();
            UUID uuid = UUID.randomUUID();
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(uuid, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.getByUuid(new UserPrincipal(user), uuid))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Address not found");
        }
    }

    @Nested
    @DisplayName("Создание адреса")
    class Create {

        @Test
        @DisplayName("Первый адрес сохраняется в базе данных выбранным и привязанным к пользователю из токена")
        void firstAddress() {
            User user = user();
            AddressRequest request = new AddressRequest();
            Address address = new Address();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.countByUser(user)).thenReturn(0L);
            when(addressMapper.toEntity(request)).thenReturn(address);

            addressService.create(new UserPrincipal(user), request);

            verify(addressRepository).save(address);
            assertThat(address.getUuid()).isNotNull();
            assertThat(address.getUser()).isSameAs(user);
            assertThat(address.isSelected()).isTrue();
            assertThat(address.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Следующий адрес сохраняется в базе данных без выбора, выбранный адрес не изменяется")
        void nextAddress() {
            User user = user();
            AddressRequest request = new AddressRequest();
            Address address = new Address();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.countByUser(user)).thenReturn(3L);
            when(addressMapper.toEntity(request)).thenReturn(address);

            addressService.create(new UserPrincipal(user), request);

            verify(addressRepository).save(address);
            assertThat(address.isSelected()).isFalse();
        }

        @Test
        @DisplayName("При десяти сохранённых адресах новый адрес отклоняется и в базу данных не сохраняется")
        void limitReached() {
            User user = user();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.countByUser(user)).thenReturn(10L);

            assertThatThrownBy(() -> addressService.create(new UserPrincipal(user), new AddressRequest()))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Address limit reached");
            verify(addressRepository, never()).save(any());
        }

        @Test
        @DisplayName("Строка пользователя блокируется в базе данных до подсчёта его адресов")
        void userLocked() {
            User user = user();
            AddressRequest request = new AddressRequest();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressMapper.toEntity(request)).thenReturn(new Address());

            addressService.create(new UserPrincipal(user), request);

            InOrder order = inOrder(userRepository, addressRepository);
            order.verify(userRepository).findLockedByUuid(user.getUuid());
            order.verify(addressRepository).countByUser(user);
            verify(userRepository, never()).findByUuid(any());
        }

        @Test
        @DisplayName("Для удалённого пользователя возвращается ошибка «не найден», адрес в базу данных не сохраняется")
        void inactiveUser() {
            User user = user();
            user.setActive(false);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> addressService.create(new UserPrincipal(user), new AddressRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
            verify(addressRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Изменение адреса")
    class Update {

        @Test
        @DisplayName("Новые данные переносятся в адрес пользователя из токена, и он возвращается в ответе")
        void found() {
            User user = user();
            Address address = address(user, false);
            AddressRequest request = new AddressRequest();
            AddressResponse expected = new AddressResponse();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(address.getUuid(), user)).thenReturn(Optional.of(address));
            when(addressMapper.toResponse(address)).thenReturn(expected);

            AddressResponse response = addressService.update(new UserPrincipal(user), address.getUuid(), request);

            verify(addressMapper).updateEntity(request, address);
            assertThat(response).isSameAs(expected);
        }

        @Test
        @DisplayName("Для чужого или несуществующего адреса возвращается ошибка «не найден», данные не изменяются")
        void addressNotFound() {
            User user = user();
            UUID uuid = UUID.randomUUID();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(uuid, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.update(new UserPrincipal(user), uuid, new AddressRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Address not found");
            verify(addressMapper, never()).updateEntity(any(), any());
        }
    }

    @Nested
    @DisplayName("Выбор адреса")
    class Select {

        @Test
        @DisplayName("Выбор снимается с остальных адресов пользователя в базе данных, после чего адрес становится выбранным")
        void notSelected() {
            User user = user();
            Address address = address(user, false);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(address.getUuid(), user)).thenReturn(Optional.of(address));

            addressService.select(new UserPrincipal(user), address.getUuid());

            verify(addressRepository).clearSelection(user, address);
            assertThat(address.isSelected()).isTrue();
        }

        @Test
        @DisplayName("Повторный выбор уже выбранного адреса не изменяет данные в базе данных")
        void alreadySelected() {
            User user = user();
            Address address = address(user, true);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(address.getUuid(), user)).thenReturn(Optional.of(address));

            addressService.select(new UserPrincipal(user), address.getUuid());

            verify(addressRepository, never()).clearSelection(any(), any());
            assertThat(address.isSelected()).isTrue();
        }

        @Test
        @DisplayName("Для чужого или несуществующего адреса возвращается ошибка «не найден», выбор не изменяется")
        void addressNotFound() {
            User user = user();
            UUID uuid = UUID.randomUUID();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(uuid, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.select(new UserPrincipal(user), uuid))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Address not found");
            verify(addressRepository, never()).clearSelection(any(), any());
        }
    }

    @Nested
    @DisplayName("Удаление адреса")
    class Delete {

        @Test
        @DisplayName("После удаления выбранного адреса из базы данных выбранным становится самый новый из оставшихся")
        void selectedAddress() {
            User user = user();
            Address address = address(user, true);
            Address newest = address(user, false);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(address.getUuid(), user)).thenReturn(Optional.of(address));
            when(addressRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(newest));

            addressService.delete(new UserPrincipal(user), address.getUuid());

            InOrder order = inOrder(addressRepository);
            order.verify(addressRepository).delete(address);
            order.verify(addressRepository).flush();
            order.verify(addressRepository).findFirstByUserOrderByCreatedAtDesc(user);
            assertThat(newest.isSelected()).isTrue();
        }

        @Test
        @DisplayName("Удаление последнего адреса проходит без ошибок, когда выбирать больше нечего")
        void lastAddress() {
            User user = user();
            Address address = address(user, true);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(address.getUuid(), user)).thenReturn(Optional.of(address));
            when(addressRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());

            addressService.delete(new UserPrincipal(user), address.getUuid());

            verify(addressRepository).delete(address);
        }

        @Test
        @DisplayName("Удаление невыбранного адреса из базы данных не изменяет выбранный адрес")
        void notSelectedAddress() {
            User user = user();
            Address address = address(user, false);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(address.getUuid(), user)).thenReturn(Optional.of(address));

            addressService.delete(new UserPrincipal(user), address.getUuid());

            verify(addressRepository).delete(address);
            verify(addressRepository, never()).findFirstByUserOrderByCreatedAtDesc(any());
        }

        @Test
        @DisplayName("Для чужого или несуществующего адреса возвращается ошибка «не найден», из базы данных ничего не удаляется")
        void addressNotFound() {
            User user = user();
            UUID uuid = UUID.randomUUID();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(uuid, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.delete(new UserPrincipal(user), uuid))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Address not found");
            verify(addressRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Для удалённого пользователя возвращается ошибка «не найден», из базы данных ничего не удаляется")
        void inactiveUser() {
            User user = user();
            user.setActive(false);
            UUID uuid = UUID.randomUUID();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> addressService.delete(new UserPrincipal(user), uuid))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
            verify(addressRepository, never()).findByUuidAndUser(any(), any());
            verify(addressRepository, never()).delete(any());
        }
    }

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setUuid(UUID.randomUUID());
        user.setEmail("ivan@mail.ru");
        user.setPassword("hash");
        user.setName("Иван");
        user.setActive(true);
        return user;
    }

    private static Address address(User user, boolean selected) {
        Address address = new Address();
        address.setUuid(UUID.randomUUID());
        address.setUser(user);
        address.setCity("Москва");
        address.setStreet("ул. Ленина");
        address.setHouse("5к2");
        address.setSelected(selected);
        address.setCreatedAt(Instant.now());
        return address;
    }
}
