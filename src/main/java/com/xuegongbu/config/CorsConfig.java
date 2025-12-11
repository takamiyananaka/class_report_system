package com.xuegongbu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域资源共享(CORS)配置类
 * 
 * 配置允许的跨域请求来源、方法和头信息
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器
     * 
     * 允许规则：
     * 1. 允许所有来源的跨域请求 (开发环境配置，生产环境建议配置具体的前端域名)
     * 2. 允许所有HTTP方法 (GET, POST, PUT, DELETE, OPTIONS等)
     * 3. 允许所有请求头
     * 4. 允许携带认证信息 (如Cookie、JWT Token等)
     * 5. 预检请求缓存时间：3600秒
     * 
     * @return CorsFilter CORS过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有来源（开发环境）
        // 生产环境建议改为具体的前端域名，例如：
        // config.addAllowedOrigin("http://localhost:3000");
        // config.addAllowedOrigin("https://your-frontend-domain.com");
        config.addAllowedOriginPattern("*");
        
        // 允许携带认证信息（Cookie、Authorization头等）
        config.setAllowCredentials(true);
        
        // 允许所有HTTP方法
        config.addAllowedMethod("*");
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 暴露的响应头，前端可以访问这些头信息
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition");
        
        // 预检请求的有效期，单位为秒（1小时）
        config.setMaxAge(3600L);
        
        // 应用CORS配置到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
