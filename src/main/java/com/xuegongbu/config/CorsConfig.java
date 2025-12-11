package com.xuegongbu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 跨域资源共享(CORS)配置类
 * 
 * 配置允许的跨域请求来源、方法和头信息
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS配置源
     * 
     * 允许规则：
     * 1. 允许本地开发环境的跨域请求 (localhost和127.0.0.1的所有端口)
     * 2. 允许常用的HTTP方法 (GET, POST, PUT, DELETE, OPTIONS, PATCH)
     * 3. 允许常用的请求头 (Authorization, Content-Type, Accept等)
     * 4. 允许携带认证信息 (如Cookie、JWT Token等)
     * 5. 预检请求缓存时间：3600秒
     * 
     * 安全说明：
     * - 默认配置仅允许本地开发环境访问
     * - 明确指定允许的HTTP方法和请求头，提高安全性
     * - 生产环境请在下方添加具体的前端域名
     * 
     * @return UrlBasedCorsConfigurationSource CORS配置源
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许本地开发环境（请根据实际需要添加或修改）
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        
        // 生产环境请取消注释并配置具体的前端域名，例如：
        // config.addAllowedOrigin("https://your-frontend-domain.com");
        // config.addAllowedOrigin("https://www.your-frontend-domain.com");
        
        // 允许携带认证信息（Cookie、Authorization头等）
        config.setAllowCredentials(true);
        
        // 允许常用的HTTP方法
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("PATCH");
        
        // 允许常用的请求头
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("Origin");
        config.addAllowedHeader("X-Requested-With");
        
        // 暴露的响应头，前端可以访问这些头信息
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition");
        
        // 预检请求的有效期，单位为秒（1小时）
        config.setMaxAge(3600L);
        
        // 应用CORS配置到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return source;
    }
}
