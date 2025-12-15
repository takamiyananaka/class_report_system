package com.xuegongbu.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 * 使用HS384算法生成和验证JWT Token
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成密钥 - 使用HS384算法
     */
    private SecretKey getSecretKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT token - 使用HS384算法
     */
    public String generateToken(String userId, String username, String teacherNo) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("teacherNo", teacherNo)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS384)
                .compact();
        
        log.info("生成JWT Token成功 - 用户ID: {}, 用户名: {}, 教师工号: {}, 过期时间: {}", userId, username, teacherNo, expiryDate);
        return token;
    }

    /**
     * 从token中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 从token中获取教师工号
     */
    public String getTeacherNoFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("teacherNo", String.class) : null;
    }

    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                log.warn("Token验证失败: 无法解析Claims");
                return false;
            }
            if (isTokenExpired(claims)) {
                log.warn("Token验证失败: Token已过期，过期时间: {}", claims.getExpiration());
                return false;
            }
            log.info("Token验证成功 - 用户ID: {}, 用户名: {}", claims.getSubject(), claims.get("username"));
            return true;
        } catch (Exception e) {
            log.error("Token验证失败: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    /**
     * 从token中获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析token失败: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * 判断token是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
}
