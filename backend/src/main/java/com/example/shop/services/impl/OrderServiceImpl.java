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
import com.example.shop.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Duration DELIVERY_FROM = Duration.ofDays(2);
    private static final Duration DELIVERY_TO = Duration.ofDays(3);

    private final OrderRepository orderRepository;
    private final CartProductRepository cartProductRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse create(UserPrincipal principal, OrderRequest request) {
        UUID userUuid = principal.getUuid();

        User user = userRepository.findLockedByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("Order creation failed: user not found, uuid={}", userUuid);
                    return new ResourceNotFoundException("User not found");
                });
        if (!user.isActive()) {
            log.warn("Order creation failed: user is deleted or not verified, uuid={}", userUuid);
            throw new ResourceNotFoundException("User not found");
        }
        Address address = addressRepository.findByUuidAndUser(request.getAddressUuid(), user)
                .orElseThrow(() -> {
                    log.warn("Order creation failed: address not found, user uuid={}, address uuid={}",
                            userUuid, request.getAddressUuid());
                    return new ResourceNotFoundException("Address not found");
                });

        List<Long> productIds = cartProductRepository.findProductIdsByUser(user);
        if (productIds.isEmpty()) {
            log.warn("Order creation warn: cart is empty, user uuid={}", userUuid);
            throw new ConflictException("Cart is empty");
        }
        productRepository.findLockedByIdInOrderByIdAsc(productIds);
        List<CartProduct> cart = cartProductRepository.findAllByUserOrderByCreatedAtDescIdDesc(user);

        Instant now = Instant.now();
        Order order = new Order();
        order.setUuid(UUID.randomUUID());
        order.setUser(user);
        order.setRecipientName(request.getRecipientName().strip());
        order.setRecipientPhone(request.getRecipientPhone());
        order.setAddress(formatAddress(address));
        order.setDeliveryFrom(now.plus(DELIVERY_FROM));
        order.setDeliveryTo(now.plus(DELIVERY_TO));
        order.setCreatedAt(now);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (CartProduct cartProduct : cart) {
            Product product = cartProduct.getProduct();
            int quantity = cartProduct.getQuantity();
            if (quantity > product.getStock()) {
                log.warn("Order creation warn: not enough stock, requested={}, stock={}, user uuid={}, product uuid={}",
                        quantity, product.getStock(), userUuid, product.getUuid());
                throw new ConflictException("Not enough stock");
            }

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrder(order);
            orderProduct.setProduct(product);
            orderProduct.setName(product.getName());
            orderProduct.setPrice(product.getPrice());
            orderProduct.setOldPrice(product.getOldPrice());
            orderProduct.setQuantity(quantity);
            order.getProducts().add(orderProduct);

            BigDecimal count = BigDecimal.valueOf(quantity);
            BigDecimal fullPrice = product.getOldPrice() != null ? product.getOldPrice() : product.getPrice();
            subtotal = subtotal.add(fullPrice.multiply(count));
            total = total.add(product.getPrice().multiply(count));
        }
        order.setSubtotal(subtotal);
        order.setTotal(total);
        order.setDiscount(subtotal.subtract(total));

        if (user.getBalance().compareTo(total) < 0) {
            log.warn("Order creation warn: not enough balance, user uuid={}", userUuid);
            throw new ConflictException("Not enough balance");
        }

        for (CartProduct cartProduct : cart) {
            Product product = cartProduct.getProduct();
            product.setStock(product.getStock() - cartProduct.getQuantity());
        }
        user.setBalance(user.getBalance().subtract(total));
        user.setUpdatedAt(now);
        orderRepository.save(order);
        cartProductRepository.deleteAllByUser(user);

        log.info("User created order, user uuid={}, order uuid={}", userUuid, order.getUuid());
        OrderResponse response = orderMapper.toResponse(order);
        response.setBalance(user.getBalance());
        return response;
    }

    private String formatAddress(Address address) {
        StringBuilder line = new StringBuilder()
                .append(address.getCity())
                .append(", ").append(address.getStreet())
                .append(", д. ").append(address.getHouse());
        if (address.getApartment() != null) {
            line.append(", кв. ").append(address.getApartment());
        }
        return line.toString();
    }
}
