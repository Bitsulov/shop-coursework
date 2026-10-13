package com.example.shop.services.impl;

import com.example.shop.dtos.order.OrderRequest;
import com.example.shop.dtos.order.OrderResponse;
import com.example.shop.exceptions.ConflictException;
import com.example.shop.exceptions.ResourceNotFoundException;
import com.example.shop.mappers.OrderMapper;
import com.example.shop.models.entities.Address;
import com.example.shop.models.entities.CartProduct;
import com.example.shop.models.entities.Order;
import com.example.shop.models.entities.OrderProduct;
import com.example.shop.models.entities.Product;
import com.example.shop.models.entities.User;
import com.example.shop.repositories.AddressRepository;
import com.example.shop.repositories.CartProductRepository;
import com.example.shop.repositories.OrderRepository;
import com.example.shop.repositories.ProductRepository;
import com.example.shop.repositories.UserRepository;
import com.example.shop.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartProductRepository cartProductRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Nested
    @DisplayName("Оформление заказа")
    class Create {

        @Test
        @DisplayName("Заказ сохраняется в базе данных с копией товаров, адреса и сумм, остатки и баланс списываются, корзина очищается")
        void created() {
            User user = user("100000.00");
            Address address = address("48");
            Product phone = product(1L, 3, "50000.00", "60000.00");
            Product kettle = product(2L, 5, "5000.00", null);
            List<CartProduct> cart = List.of(cartProduct(user, phone, 1), cartProduct(user, kettle, 2));
            stubOrder(user, address, cart);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());

            OrderResponse response = orderService.create(new UserPrincipal(user), request(address));

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            Order order = captor.getValue();
            assertThat(order.getUuid()).isNotNull();
            assertThat(order.getUser()).isSameAs(user);
            assertThat(order.getRecipientName()).isEqualTo("Анна Смирнова");
            assertThat(order.getAddress()).isEqualTo("Москва, ул. Лесная, д. 5к2, кв. 48");
            assertThat(order.getSubtotal()).isEqualByComparingTo("70000.00");
            assertThat(order.getDiscount()).isEqualByComparingTo("10000.00");
            assertThat(order.getTotal()).isEqualByComparingTo("60000.00");
            assertThat(Duration.between(order.getCreatedAt(), order.getDeliveryFrom())).isEqualTo(Duration.ofDays(2));
            assertThat(Duration.between(order.getCreatedAt(), order.getDeliveryTo())).isEqualTo(Duration.ofDays(3));
            assertThat(order.getProducts())
                    .extracting(OrderProduct::getName, OrderProduct::getQuantity)
                    .containsExactly(
                            tuple("Товар 1", 1),
                            tuple("Товар 2", 2));
            assertThat(phone.getStock()).isEqualTo(2);
            assertThat(kettle.getStock()).isEqualTo(3);
            assertThat(user.getBalance()).isEqualByComparingTo("40000.00");
            assertThat(response.getBalance()).isEqualByComparingTo("40000.00");
            verify(cartProductRepository).deleteAllByUser(user);
        }

        @Test
        @DisplayName("Товары блокируются в базе данных до загрузки корзины")
        void productsLockedFirst() {
            User user = user("100000.00");
            Address address = address("48");
            Product phone = product(1L, 3, "50000.00", null);
            stubOrder(user, address, List.of(cartProduct(user, phone, 1)));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());

            orderService.create(new UserPrincipal(user), request(address));

            InOrder order = inOrder(productRepository, cartProductRepository);
            order.verify(productRepository).findLockedByIdInOrderByIdAsc(List.of(1L));
            order.verify(cartProductRepository).findAllByUserOrderByCreatedAtDescIdDesc(user);
        }

        @Test
        @DisplayName("Адрес без квартиры сохраняется в заказе без части «кв.»")
        void addressWithoutApartment() {
            User user = user("100000.00");
            Address address = address(null);
            Product phone = product(1L, 3, "50000.00", null);
            stubOrder(user, address, List.of(cartProduct(user, phone, 1)));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());

            orderService.create(new UserPrincipal(user), request(address));

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getAddress()).isEqualTo("Москва, ул. Лесная, д. 5к2");
        }

        @Test
        @DisplayName("При пустой корзине заказ не сохраняется в базе данных")
        void emptyCart() {
            User user = user("100000.00");
            Address address = address("48");
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(address.getUuid(), user)).thenReturn(Optional.of(address));
            when(cartProductRepository.findProductIdsByUser(user)).thenReturn(List.of());

            assertThatThrownBy(() -> orderService.create(new UserPrincipal(user), request(address)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Cart is empty");
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("При нехватке товара на складе заказ не сохраняется, остатки и баланс не меняются")
        void notEnoughStock() {
            User user = user("100000.00");
            Address address = address("48");
            Product phone = product(1L, 1, "50000.00", null);
            stubOrder(user, address, List.of(cartProduct(user, phone, 2)));

            assertThatThrownBy(() -> orderService.create(new UserPrincipal(user), request(address)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Not enough stock");
            assertThat(phone.getStock()).isEqualTo(1);
            assertThat(user.getBalance()).isEqualByComparingTo("100000.00");
            verify(orderRepository, never()).save(any());
            verify(cartProductRepository, never()).deleteAllByUser(any());
        }

        @Test
        @DisplayName("При нехватке баланса заказ не сохраняется, остатки и баланс не меняются")
        void notEnoughBalance() {
            User user = user("1000.00");
            Address address = address("48");
            Product phone = product(1L, 3, "50000.00", null);
            stubOrder(user, address, List.of(cartProduct(user, phone, 1)));

            assertThatThrownBy(() -> orderService.create(new UserPrincipal(user), request(address)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Not enough balance");
            assertThat(phone.getStock()).isEqualTo(3);
            assertThat(user.getBalance()).isEqualByComparingTo("1000.00");
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Для чужого или несуществующего адреса возвращается ошибка «не найден», корзина не затрагивается")
        void addressNotFound() {
            User user = user("100000.00");
            Address address = address("48");
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
            when(addressRepository.findByUuidAndUser(address.getUuid(), user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.create(new UserPrincipal(user), request(address)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Address not found");
            verify(cartProductRepository, never()).findProductIdsByUser(any());
        }

        @Test
        @DisplayName("Для удалённого пользователя возвращается ошибка «не найден», заказ не сохраняется")
        void inactiveUser() {
            User user = user("100000.00");
            user.setActive(false);
            when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> orderService.create(new UserPrincipal(user), request(address("48"))))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
            verify(orderRepository, never()).save(any());
        }
    }

    private void stubOrder(User user, Address address, List<CartProduct> cart) {
        List<Long> ids = cart.stream().map(cartProduct -> cartProduct.getProduct().getId()).toList();
        when(userRepository.findLockedByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(addressRepository.findByUuidAndUser(address.getUuid(), user)).thenReturn(Optional.of(address));
        when(cartProductRepository.findProductIdsByUser(user)).thenReturn(ids);
        when(cartProductRepository.findAllByUserOrderByCreatedAtDescIdDesc(user)).thenReturn(cart);
    }

    private static User user(String balance) {
        User user = new User();
        user.setId(1L);
        user.setUuid(UUID.randomUUID());
        user.setEmail("anna@mail.ru");
        user.setPassword("hash");
        user.setName("Анна");
        user.setBalance(new BigDecimal(balance));
        user.setActive(true);
        return user;
    }

    private static Address address(String apartment) {
        Address address = new Address();
        address.setUuid(UUID.randomUUID());
        address.setCity("Москва");
        address.setStreet("ул. Лесная");
        address.setHouse("5к2");
        address.setApartment(apartment);
        return address;
    }

    private static Product product(Long id, int stock, String price, String oldPrice) {
        Product product = new Product();
        product.setId(id);
        product.setUuid(UUID.randomUUID());
        product.setName("Товар " + id);
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
        return cartProduct;
    }

    private static OrderRequest request(Address address) {
        OrderRequest request = new OrderRequest();
        request.setRecipientName("Анна Смирнова");
        request.setRecipientPhone("+79123456789");
        request.setAddressUuid(address.getUuid());
        return request;
    }
}
