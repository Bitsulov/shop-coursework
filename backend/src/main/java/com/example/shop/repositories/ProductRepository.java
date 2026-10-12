package com.example.shop.repositories;

import com.example.shop.models.entities.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "brand", "specs"})
    Optional<Product> findByUuid(UUID uuid);
}
