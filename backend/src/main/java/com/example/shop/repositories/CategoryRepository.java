package com.example.shop.repositories;

import com.example.shop.models.entities.Category;
import com.example.shop.models.enums.CategoryEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(CategoryEnum name);
}
