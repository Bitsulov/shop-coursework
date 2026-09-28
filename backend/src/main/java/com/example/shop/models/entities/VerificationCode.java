package com.example.shop.models.entities;

import com.example.shop.models.enums.VerificationCodesType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "verification_codes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "code_type"})
)
public class VerificationCode extends BaseEntity {

    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false)
    private Instant codeExpiresAt;

    @Column(nullable = false)
    private String targetValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "code_type", nullable = false, length = 30)
    private VerificationCodesType type;

    @Column(nullable = false)
    private int attempts;
}
