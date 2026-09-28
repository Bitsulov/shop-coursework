package com.example.shop.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private SecretKey key;

    @Value("${app.jwt.secret}")
    private String secret;

    @Getter
    @Value("${app.jwt.access-token-validity}")
    private long accessTokenValidityInMillis;

    @Getter
    @Value("${app.jwt.refresh-token-validity}")
    private long refreshTokenValidityInMillis;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID uuid) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidityInMillis);

        return Jwts.builder()
                .subject(uuid.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("type", "access")
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UUID uuid) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidityInMillis);

        return Jwts.builder()
                .subject(uuid.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("type", "refresh")
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT rejected: {}", ex.getMessage());
        }
        return false;
    }

    public String getUsernameFromJWT(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getTokenType(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
    }
}
