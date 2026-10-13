package com.example.shop.services.impl;

import com.example.shop.dtos.cart.CartQuantityRequest;
import com.example.shop.dtos.cart.CartResponse;
import com.example.shop.dtos.product.ProductCardResponse;
import com.example.shop.exceptions.ConflictException;
import com.example.shop.exceptions.ResourceNotFoundException;
import com.example.shop.mappers.ProductMapper;
import com.example.shop.models.entities.CartProduct;
import com.example.shop.models.entities.Product;
import com.example.shop.models.entities.User;
import com.example.shop.repositories.CartProductRepository;
import com.example.shop.repositories.ProductRepository;
import com.example.shop.repositories.UserRepository;
import com.example.shop.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartProductRepository cartProductRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    @Nested
    @DisplayName("Получение корзины")
    class Get {

        @Test
        @DisplayName("Суммы считаются по товарам из базы данных: без скидки по старой цене, итог по текущей")
        void totals() {
            User user = user();
            Product phone = product(10, "50000.00", "60000.00");
            Product kettle = product(10, "5000.00", null);
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(cartProductRepository.findAllByUserOrderByCreatedAtDescIdDesc(user))
                    .thenReturn(List.of(cartProduct(user, phone, 2), cartProduct(user, kettle, 1)));
            when(productMapper.toCardResponse(any(Product.class))).thenReturn(new ProductCardResponse());

            CartResponse response = cartService.get(new UserPrincipal(user));

            assertThat(response.getProducts()).hasSize(2);
            assertThat(response.getProducts().get(0).getTotal()).isEqualByComparingTo("100000.00");
            assertThat(response.getTotalQuantity()).isEqualTo(3);
            assertThat(response.getSubtotal()).isEqualByComparingTo("125000.00");
            assertThat(response.getDiscount()).isEqualByComparingTo("20000.00");
            assertThat(response.getTotal()).isEqualByComparingTo("105000.00");
        }

        @Test
        @DisplayName("При отсутствии пользователя из токена в базе данных возвращается ошибка «не найден»")
        void userNotFound() {
            User user = user();
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.get(new UserPrincipal(user)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("Количество товара в корзине")
    class SetQuantity {

        @Test
        @DisplayName("Товар, которого нет в корзине, сохраняется в базе данных с количеством из запроса")
        void newProduct() {
            User user = user();
            Product product = product(5, "50000.00", null);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(productRepository.findByUuid(product.getUuid())).thenReturn(Optional.of(product));
            when(cartProductRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
            when(cartProductRepository.countByUser(user)).thenReturn(3L);

            cartService.setQuantity(new UserPrincipal(user), product.getUuid(), quantity(2));

            ArgumentCaptor<CartProduct> captor = ArgumentCaptor.forClass(CartProduct.class);
            verify(cartProductRepository).saveAndFlush(captor.capture());
            assertThat(captor.getValue().getUser()).isSameAs(user);
            assertThat(captor.getValue().getProduct()).isSameAs(product);
            assertThat(captor.getValue().getQuantity()).isEqualTo(2);
            assertThat(captor.getValue().getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("У товара, который уже есть в корзине, в базе данных меняется только количество")
        void existingProduct() {
            User user = user();
            Product product = product(5, "50000.00", null);
            CartProduct cartProduct = cartProduct(user, product, 1);
            Instant createdAt = cartProduct.getCreatedAt();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(productRepository.findByUuid(product.getUuid())).thenReturn(Optional.of(product));
            when(cartProductRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(cartProduct));

            cartService.setQuantity(new UserPrincipal(user), product.getUuid(), quantity(4));

            assertThat(cartProduct.getQuantity()).isEqualTo(4);
            assertThat(cartProduct.getCreatedAt()).isEqualTo(createdAt);
            verify(cartProductRepository).saveAndFlush(cartProduct);
            verify(cartProductRepository, never()).countByUser(any());
        }

        @Test
        @DisplayName("Количество больше остатка на складе отклоняется без изменений в базе данных")
        void notEnoughStock() {
            User user = user();
            Product product = product(2, "50000.00", null);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(productRepository.findByUuid(product.getUuid())).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> cartService.setQuantity(new UserPrincipal(user), product.getUuid(), quantity(3)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Not enough stock");
            verify(cartProductRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Новый товар при 50 товарах в корзине отклоняется без изменений в базе данных")
        void limitReached() {
            User user = user();
            Product product = product(5, "50000.00", null);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(productRepository.findByUuid(product.getUuid())).thenReturn(Optional.of(product));
            when(cartProductRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
            when(cartProductRepository.countByUser(user)).thenReturn(50L);

            assertThatThrownBy(() -> cartService.setQuantity(new UserPrincipal(user), product.getUuid(), quantity(1)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Cart product limit reached");
            verify(cartProductRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Для несуществующего товара возвращается ошибка «не найден»")
        void productNotFound() {
            User user = user();
            UUID productUuid = UUID.randomUUID();
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(productRepository.findByUuid(productUuid)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.setQuantity(new UserPrincipal(user), productUuid, quantity(1)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found");
        }

        @Test
        @DisplayName("Для удалённого пользователя возвращается ошибка «не найден», корзина в базе данных не изменяется")
        void inactiveUser() {
            User user = user();
            user.setActive(false);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> cartService.setQuantity(new UserPrincipal(user), UUID.randomUUID(), quantity(1)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
            verify(cartProductRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("Удаление товара из корзины")
    class Remove {

        @Test
        @DisplayName("Товар удаляется из корзины в базе данных")
        void removed() {
            User user = user();
            Product product = product(5, "50000.00", null);
            CartProduct cartProduct = cartProduct(user, product, 1);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(productRepository.findByUuid(product.getUuid())).thenReturn(Optional.of(product));
            when(cartProductRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(cartProduct));

            cartService.remove(new UserPrincipal(user), product.getUuid());

            verify(cartProductRepository).delete(cartProduct);
        }

        @Test
        @DisplayName("Для товара, которого нет в корзине, возвращается ошибка «не найден»")
        void notInCart() {
            User user = user();
            Product product = product(5, "50000.00", null);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(productRepository.findByUuid(product.getUuid())).thenReturn(Optional.of(product));
            when(cartProductRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.remove(new UserPrincipal(user), product.getUuid()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not in cart");
            verify(cartProductRepository, never()).delete(any(CartProduct.class));
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

    private static Product product(int stock, String price, String oldPrice) {
        Product product = new Product();
        product.setUuid(UUID.randomUUID());
        product.setName("Товар");
        product.setStock(stock);
        product.setPrice(new BigDecimal(price));
        product.setOldPrice(oldPrice == null ? null : new BigDecimal(oldPrice));
        return product;
    }

    private static CartProduct cartProduct(User user, Product product, int quantity) {
        CartProduct cartProduct = new CartProduct();
        cartProduct.setUser(user);
        cartProduct.setProduct(product);
        cartProduct.setQuantity(quantity);
        cartProduct.setCreatedAt(Instant.parse("2026-10-13T10:00:00Z"));
        return cartProduct;
    }

    private static CartQuantityRequest quantity(int quantity) {
        CartQuantityRequest request = new CartQuantityRequest();
        request.setQuantity(quantity);
        return request;
    }
}
