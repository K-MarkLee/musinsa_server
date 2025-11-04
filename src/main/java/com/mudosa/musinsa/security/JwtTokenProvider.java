// com.mudosa.musinsa.security.JwtTokenProvider
package com.mudosa.musinsa.security;

import com.mudosa.musinsa.exception.CustomJwtException;
import com.mudosa.musinsa.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = decodeSecret(jwtSecret);      // ★ 수정
        if (keyBytes.length < 32) {                     // HS256 권장 최소 32바이트
            throw new IllegalArgumentException("JWT secret too short (<32 bytes). Use 32+ bytes for HS256.");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ★ 추가: Base64 → Base64URL → Plain(UTF-8) 순서로 시도
    private byte[] decodeSecret(String src) {
        String s = src == null ? "" : src.trim();

        // 1) 표준 Base64
        try { return Decoders.BASE64.decode(s); } catch (Exception ignored) {}

        // 2) Base64URL ( '-' , '_' 허용 )
        try { return Decoders.BASE64URL.decode(s); } catch (Exception ignored) {}

        // 3) Plain UTF-8
        return s.getBytes(StandardCharsets.UTF_8);
    }

    /* accessToken 생성 */
    public String createToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)              // jjwt 0.12+ : key에서 alg 유추
                .compact();
    }

    /* refreshToken 생성 */
    public String createRefreshToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public long getRemainingExpiration(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw new CustomJwtException(ErrorCode.INVALID_JWT);
        } catch (ExpiredJwtException e) {
            throw new CustomJwtException(ErrorCode.EXPIRED_JWT);
        } catch (UnsupportedJwtException e) {
            throw new CustomJwtException(ErrorCode.UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            throw new CustomJwtException(ErrorCode.EMPTY_JWT);
        }
    }

    public String getUserIdFromJWt(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
        return claims.get("role", String.class);
    }
}
