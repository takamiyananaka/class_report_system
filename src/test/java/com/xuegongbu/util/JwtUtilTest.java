package com.xuegongbu.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "class-report-system-jwt-secret-key-for-token-generation-2025-test-version-long-enough";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
    }

    @Test
    void testGenerateToken() {
        // Given
        String userId = "123";
        String username = "testuser";
        String teacherNo = "T001";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        String token = jwtUtil.generateToken(userId, username, teacherNo);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify Redis was called to store activity
        verify(valueOperations).set(anyString(), anyLong(), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void testGetUserIdFromToken() {
        // Given
        String userId = "123";
        String username = "testuser";
        String teacherNo = "T001";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String token = jwtUtil.generateToken(userId, username, teacherNo);

        // When
        String extractedUserId = jwtUtil.getUserIdFromToken(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testGetUsernameFromToken() {
        // Given
        String userId = "123";
        String username = "testuser";
        String teacherNo = "T001";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String token = jwtUtil.generateToken(userId, username, teacherNo);

        // When
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetTeacherNoFromToken() {
        // Given
        String userId = "123";
        String username = "testuser";
        String teacherNo = "T001";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String token = jwtUtil.generateToken(userId, username, teacherNo);

        // When
        String extractedTeacherNo = jwtUtil.getTeacherNoFromToken(token);

        // Then
        assertEquals(teacherNo, extractedTeacherNo);
    }

    @Test
    void testValidateToken_WithActiveToken() {
        // Given
        String userId = "123";
        String username = "testuser";
        String teacherNo = "T001";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String token = jwtUtil.generateToken(userId, username, teacherNo);
        
        // Mock Redis to return true (token is active)
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WithInactiveToken() {
        // Given
        String userId = "123";
        String username = "testuser";
        String teacherNo = "T001";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String token = jwtUtil.generateToken(userId, username, teacherNo);
        
        // Mock Redis to return false (token is not active - expired due to inactivity)
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testUpdateTokenActivity() {
        // Given
        String token = "test-token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        jwtUtil.updateTokenActivity(token);

        // Then
        verify(valueOperations).set(contains("token:activity:"), anyLong(), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void testInvalidateToken() {
        // Given
        String token = "test-token";

        // When
        jwtUtil.invalidateToken(token);

        // Then
        verify(redisTemplate).delete(contains("token:activity:"));
    }

    @Test
    void testValidateToken_WithInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WithNullToken() {
        // When
        boolean isValid = jwtUtil.validateToken(null);

        // Then
        assertFalse(isValid);
    }
}
