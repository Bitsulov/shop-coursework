package com.example.shop.repositories;

import com.example.shop.models.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Modifying
    @Query(value = "DELETE FROM orders WHERE user_id = :userId", nativeQuery = true)
    int deleteAllByUserId(@Param("userId") Long userId);
}
