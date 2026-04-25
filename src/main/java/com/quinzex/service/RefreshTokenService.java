package com.quinzex.service;

import com.quinzex.dto.RefreshTokenData;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService implements IrefreshTokenService {
    private static final long REFRESH_EXP_MS = 7 * 24 * 60 * 60 * 1000;
    private final RedisTemplate<String,Object> redisTemplate;
    public RefreshTokenService(RedisTemplate<String,Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    private String key(String token){
        return "refresh:"+token;

    }

    @Override
    public String createRefreshToken(String email, int tokenVersion) {
        try {
            String refreshToken = UUID.randomUUID().toString();
            RefreshTokenData data = new RefreshTokenData(email,tokenVersion);


            redisTemplate.opsForValue().set(key(refreshToken),data,REFRESH_EXP_MS, TimeUnit.MILLISECONDS);

            return refreshToken;
        }catch (Exception e){
            throw new RuntimeException("Failed to create refresh token", e);
        }

    }

    @Override
    public RefreshTokenData validate(String refreshToken) {
        try {
            RefreshTokenData data = (RefreshTokenData) redisTemplate.opsForValue().get(key(refreshToken));

            if (data == null) {
                throw new RuntimeException("Invalid or expired refresh token");
            }

            return  data;

        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token", e);
        }
    }
    @Override
    public void revoke (String refreshToken){

        if (refreshToken != null && !refreshToken.isBlank()) {
            redisTemplate.delete(key(refreshToken));
        }
    }
}
