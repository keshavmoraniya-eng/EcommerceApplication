package com.ecommerce.service;

import com.ecommerce.entity.RefreshToken;
import com.ecommerce.entity.User;
import com.ecommerce.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDurationMs; // 24 hours

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, @Value("${jwt.refresh-expiration-ms}") long refreshTokenDurationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDurationMs = refreshTokenDurationMs;
    }

    public RefreshToken createRefreshToken(User user){
        RefreshToken refreshToken=new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isExpired(RefreshToken token){
        return token.getExpiryDate().isBefore(Instant.now());
    }

    public int deleteByUser(User user){
        return refreshTokenRepository.deleteByUser(user);
    }

}
