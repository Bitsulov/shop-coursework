package com.example.shop.services.impl;

import com.example.shop.dtos.cart.CartProductResponse;
import com.example.shop.dtos.cart.CartQuantityRequest;
import com.example.shop.dtos.cart.CartResponse;
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
import com.example.shop.services.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final int MAX_CART_PRODUCTS = 50;

    private final CartProductRepository cartProductRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse get(UserPrincipal principal) {
        UUID userUuid = principal.getUuid();

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("Getting cart failed: user not found, uuid={}", userUuid);
                    return new ResourceNotFoundException("User not found");
                });

        return buildResponse(user);
    }

    @Override
    @Transactional
    public CartResponse setQuantity(UserPrincipal principal, UUID productUuid, CartQuantityRequest request) {
        UUID userUuid = principal.getUuid();
        int quantity = request.getQuantity();

        User user = findActiveLockedUser(userUuid, "Cart update");
        Product product = productRepository.findByUuid(productUuid)
                .orElseThrow(() -> {
                    log.warn("Cart update failed: product not found, user uuid={}, product uuid={}", userUuid, productUuid);
                    return new ResourceNotFoundException("Product not found");
                });
        if (quantity > product.getStock()) {
            log.warn("Cart update warn: not enough stock, requested={}, stock={}, user uuid={}, product uuid={}",
                    quantity, product.getStock(), userUuid, productUuid);
            throw new ConflictException("Not enough stock");
        }

        CartProduct cartProduct = cartProductRepository.findByUserAndProduct(user, product).orElse(null);
        if (cartProduct == null) {
            if (cartProductRepository.countByUser(user) >= MAX_CART_PRODUCTS) {
                log.warn("Cart update warn: limit of {} products reached, user uuid={}", MAX_CART_PRODUCTS, userUuid);
                throw new ConflictException("Cart product limit reached");
            }
            cartProduct = new CartProduct();
            cartProduct.setUser(user);
            cartProduct.setProduct(product);
            cartProduct.setCreatedAt(Instant.now());
        }
        cartProduct.setQuantity(quantity);
        cartProductRepository.saveAndFlush(cartProduct);

        log.info("User set product quantity in cart, quantity={}, user uuid={}, product uuid={}", quantity, userUuid, productUuid);
        return buildResponse(user);
    }

    @Override
    @Transactional
    public CartResponse remove(UserPrincipal principal, UUID productUuid) {
        UUID userUuid = principal.getUuid();

        User user = findActiveLockedUser(userUuid, "Cart removal");
        Product product = productRepository.findByUuid(productUuid)
                .orElseThrow(() -> {
                    log.warn("Cart removal failed: product not found, user uuid={}, product uuid={}", userUuid, productUuid);
                    return new ResourceNotFoundException("Product not found");
                });
        CartProduct cartProduct = cartProductRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> {
                    log.warn("Cart removal failed: product not in cart, user uuid={}, product uuid={}", userUuid, productUuid);
                    return new ResourceNotFoundException("Product not in cart");
                });

        cartProductRepository.delete(cartProduct);
        cartProductRepository.flush();

        log.info("User removed product from cart, user uuid={}, product uuid={}", userUuid, productUuid);
        return buildResponse(user);
    }

    private User findActiveLockedUser(UUID userUuid, String action) {
        User user = userRepository.findLockedByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("{} failed: user not found, uuid={}", action, userUuid);
                    return new ResourceNotFoundException("User not found");
                });
        if (!user.isActive()) {
            log.warn("{} failed: user is deleted or not verified, uuid={}", action, userUuid);
            throw new ResourceNotFoundException("User not found");
        }
        return user;
    }

    private CartResponse buildResponse(User user) {
        List<CartProductResponse> products = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (CartProduct cartProduct : cartProductRepository.findAllByUserOrderByCreatedAtDescIdDesc(user)) {
            Product product = cartProduct.getProduct();
            BigDecimal quantity = BigDecimal.valueOf(cartProduct.getQuantity());
            BigDecimal fullPrice = product.getOldPrice() != null ? product.getOldPrice() : product.getPrice();
            BigDecimal productTotal = product.getPrice().multiply(quantity);

            CartProductResponse response = new CartProductResponse();
            response.setProduct(productMapper.toCardResponse(product));
            response.setQuantity(cartProduct.getQuantity());
            response.setTotal(productTotal);
            products.add(response);

            totalQuantity += cartProduct.getQuantity();
            subtotal = subtotal.add(fullPrice.multiply(quantity));
            total = total.add(productTotal);
        }

        CartResponse response = new CartResponse();
        response.setProducts(products);
        response.setTotalQuantity(totalQuantity);
        response.setSubtotal(subtotal);
        response.setDiscount(subtotal.subtract(total));
        response.setTotal(total);
        return response;
    }
}
