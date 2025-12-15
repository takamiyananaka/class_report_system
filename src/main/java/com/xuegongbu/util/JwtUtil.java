package com.xuegongbu.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类
 * 使用HS384算法生成和验证JWT Token
 * 支持基于活动的Token过期（24小时无操作后过期）
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String TOKEN_ACTIVITY_PREFIX = "token:activity:";
    private static final long ACTIVITY_TIMEOUT_HOURS = 24;

    /**
     * 生成密钥 - 使用HS384算法
     */
    private SecretKey getSecretKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT token - 使用HS384算法
     * Token本身设置较长的过期时间，实际过期由Redis活动记录控制
     */
    public String generateToken(String userId, String username, String teacherNo) {
        Date now = new Date();
        // Token本身设置7天过期，实际过期由Redis控制（24小时无活动）
        Date expiryDate = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000L);

        String token = Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("teacherNo", teacherNo)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS384)
                .compact();
        
        // 在Redis中记录token的活动时间，设置24小时过期
        updateTokenActivity(token);
        
        log.info("生成JWT Token成功 - 用户ID: {}, 用户名: {}, 教师工号: {}, JWT过期时间: {}, Redis活动超时: {}小时", 
                userId, username, teacherNo, expiryDate, ACTIVITY_TIMEOUT_HOURS);
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
     * 同时检查JWT本身的过期时间和Redis中的活动记录
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
            
            // 检查Redis中的活动记录
            if (!isTokenActive(token)) {
                log.warn("Token验证失败: 超过{}小时无操作，需要重新登录", ACTIVITY_TIMEOUT_HOURS);
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
     * 检查token在Redis中是否仍然活跃
     */
    private boolean isTokenActive(String token) {
        try {
            String key = TOKEN_ACTIVITY_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查Token活动状态失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新token的活动时间
     * 每次请求都会调用，刷新Redis中的过期时间
     */
    public void updateTokenActivity(String token) {
        try {
            String key = TOKEN_ACTIVITY_PREFIX + token;
            // 设置或更新活动时间，过期时间为24小时
            redisTemplate.opsForValue().set(key, System.currentTimeMillis(), ACTIVITY_TIMEOUT_HOURS, TimeUnit.HOURS);
            log.debug("更新Token活动时间，剩余有效期: {}小时", ACTIVITY_TIMEOUT_HOURS);
        } catch (Exception e) {
            log.error("更新Token活动时间失败: {}", e.getMessage());
        }
    }
    
    /**
     * 使token失效（用于登出）
     */
    public void invalidateToken(String token) {
        try {
            String key = TOKEN_ACTIVITY_PREFIX + token;
            redisTemplate.delete(key);
            log.info("Token已失效");
        } catch (Exception e) {
            log.error("使Token失效失败: {}", e.getMessage());
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
