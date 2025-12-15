package com.xuegongbu.filter;

import com.xuegongbu.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(request);
    }

    @Test
    void testShouldNotFilter_WebJarsPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/webjars/css/chunk-75464e7e.8fb93ba5.css");
        
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        
        assertTrue(result, "WebJars paths should not be filtered");
    }

    @Test
    void testShouldNotFilter_FaviconPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/favicon.ico");
        
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        
        assertTrue(result, "Favicon path should not be filtered");
    }

    @Test
    void testShouldNotFilter_SwaggerPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        
        assertTrue(result, "Swagger paths should not be filtered");
    }

    @Test
    void testShouldNotFilter_FrontLoginPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/front/login");
        
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        
        assertTrue(result, "Front login path should not be filtered");
    }

    @Test
    void testShouldNotFilter_AdminLoginPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/admin/login");
        
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        
        assertTrue(result, "Admin login path should not be filtered");
    }

    @Test
    void testShouldNotFilter_ApiDocPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/v3/api-docs/swagger-config");
        
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        
        assertTrue(result, "API docs paths should not be filtered");
    }

    @Test
    void testShouldFilter_AuthenticatedPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/course/list");
        
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        
        assertFalse(result, "Course paths should be filtered");
    }

    @Test
    void testShouldFilter_TeacherPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/teacher/profile");
        
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        
        assertFalse(result, "Teacher paths should be filtered");
    }

    @Test
    void testShouldFilter_AdminPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/admin/dashboard");
        
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        
        assertFalse(result, "Admin paths (except login) should be filtered");
    }

    @Test
    void testShouldNotFilter_ClassDownloadTemplate() throws Exception {
        when(request.getRequestURI()).thenReturn("/class/downloadTemplate");
        
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        
        assertTrue(result, "Class download template path should not be filtered");
    }
}
