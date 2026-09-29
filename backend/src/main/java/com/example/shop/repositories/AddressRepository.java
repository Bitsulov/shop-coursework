package com.example.shop.repositories;

import com.example.shop.models.entities.Address;
import com.example.shop.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByUserOrderBySelectedDescCreatedAtDesc(User user);

    Optional<Address> findByUuidAndUser(UUID uuid, User user);

    Optional<Address> findFirstByUserOrderByCreatedAtDesc(User user);

    long countByUser(User user);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE Address a SET a.selected = false WHERE a.user = :user AND a.selected = true AND a <> :target")
    int clearSelection(@Param("user") User user, @Param("target") Address target);
}
