package com.example.shop.repositories;

import com.example.shop.models.entities.CartProduct;
import com.example.shop.models.entities.Product;
import com.example.shop.models.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartProductRepository extends JpaRepository<CartProduct, Long> {

    @EntityGraph(attributePaths = "product")
    List<CartProduct> findAllByUserOrderByCreatedAtDescIdDesc(User user);

    Optional<CartProduct> findByUserAndProduct(User user, Product product);

    long countByUser(User user);

    @Query("SELECT c.product.id FROM CartProduct c WHERE c.user = :user")
    List<Long> findProductIdsByUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM CartProduct c WHERE c.user = :user")
    int deleteAllByUser(@Param("user") User user);
}
