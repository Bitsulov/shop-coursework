package com.example.shop.repositories;

import com.example.shop.models.entities.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "role")
    Optional<User> findByUuid(UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findLockedByUuid(UUID uuid);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Override
    @EntityGraph(attributePaths = "role")
    Page<User> findAll(Pageable pageable);
}
