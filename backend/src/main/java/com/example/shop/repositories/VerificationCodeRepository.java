package com.example.shop.repositories;

import com.example.shop.models.entities.User;
import com.example.shop.models.entities.VerificationCode;
import com.example.shop.models.enums.VerificationCodesType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByUserAndType(User user, VerificationCodesType type);

    @Modifying
    @Query("UPDATE VerificationCode c SET c.attempts = c.attempts + 1 WHERE c.id = :id AND c.attempts < :maxAttempts")
    int incrementAttempts(@Param("id") Long id, @Param("maxAttempts") int maxAttempts);

    @Modifying
    @Query("DELETE FROM VerificationCode c WHERE c.user = :user")
    int deleteAllByUser(@Param("user") User user);
}
