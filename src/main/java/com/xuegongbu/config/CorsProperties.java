package com.xuegongbu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "cors.allowed")
public class CorsProperties {
    
    /**
     * 允许的跨域源列表
     */
    private List<String> origins = new ArrayList<>();
}
