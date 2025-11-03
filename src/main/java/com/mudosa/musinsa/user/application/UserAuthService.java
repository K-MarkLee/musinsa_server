package com.mudosa.musinsa.user.application;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.security.JwtTokenProvider;
import com.mudosa.musinsa.user.controller.dto.LoginRequest;
import com.mudosa.musinsa.user.controller.dto.TokenResponse;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_KEY_PREFIX = "user:";
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";

    public TokenResponse login(@Valid LoginRequest request) {
        User user = userRepository.findByUserEmail(request.getEmail()).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    public TokenResponse refreshToken(String providedRefreshToken) {
        jwtTokenProvider.validateToken(providedRefreshToken);

        String userId = jwtTokenProvider.getUserIdFromJWt(providedRefreshToken);

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return issueTokens(user);
    }

    public void logout(String refreshToken, String accessToken) {
        String userId = jwtTokenProvider.getUserIdFromJWt(refreshToken);

        redisTemplate.delete(USER_KEY_PREFIX + userId);

        long expiration = jwtTokenProvider.getRemainingExpiration(accessToken);
        if (expiration > 0) {
            String key = BLACKLIST_KEY_PREFIX + hashToken(accessToken);
            redisTemplate.opsForValue().set(key, "logout", expiration, TimeUnit.MILLISECONDS);
        }
    }

    private TokenResponse issueTokens(User user) {
        String accessToken =  jwtTokenProvider.createToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getRole().name());
        redisTemplate.opsForValue().set(USER_KEY_PREFIX + user.getId(), refreshToken, Duration.ofDays(7));

        return new TokenResponse(accessToken, refreshToken);
    }

    private String hashToken(String token) {
        return Integer.toHexString(token.hashCode());
    }
}
