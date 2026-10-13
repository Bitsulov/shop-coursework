package com.example.shop.repositories;

import com.example.shop.models.entities.Category;
import com.example.shop.models.entities.Product;
import com.example.shop.models.enums.CategoryEnum;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = {"category", "brand", "specs"})
    Optional<Product> findByUuid(UUID uuid);

    List<Product> findTop4ByCategoryAndIdNotOrderByCreatedAtDescIdDesc(Category category, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Product> findLockedByIdInOrderByIdAsc(Collection<Long> ids);

    @Query("""
            SELECT DISTINCT p.brand.name FROM Product p
            WHERE :category IS NULL OR p.category.name = :category
            ORDER BY p.brand.name
            """)
    List<String> findBrandNames(@Param("category") CategoryEnum category);

    @Query("""
            SELECT DISTINCT p.color FROM Product p
            WHERE p.color IS NOT NULL AND (:category IS NULL OR p.category.name = :category)
            ORDER BY p.color
            """)
    List<String> findColors(@Param("category") CategoryEnum category);
}
