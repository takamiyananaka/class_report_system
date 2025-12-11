package com.xuegongbu.config;

import com.xuegongbu.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 * 
 * 配置JWT认证和路径访问权限
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

   @Autowired
   private JwtAuthenticationFilter jwtAuthenticationFilter;

   /**
    * 配置安全过滤链
    * 
    * 路径认证规则：
    * 1. Knife4j文档路径 - 允许匿名访问
    * 2. 登录接口 (/front/login, /admin/login) - 允许匿名访问
    * 3. 下载模板接口 (/courseSchedule/downloadTemplate) - 允许匿名访问
    * 4. 其他业务接口 (/course/**, /courseSchedule/**, /teacher/**, /admin/**) - 需要JWT认证
    * 5. 其他路径 - 允许匿名访问
    * 
    * CORS配置：
    * - 启用跨域资源共享，使用CorsConfig中定义的规则
    * - 允许前端应用跨域访问API
    * 
    * JWT认证：
    * - 通过 JwtAuthenticationFilter 过滤器验证 Authorization Header 中的 Bearer Token
    * - Token验证成功后，将用户信息设置到 SecurityContext 中
    * - 后续请求可以通过 SecurityContextHolder 获取当前用户信息
    */
   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       http.authorizeHttpRequests(auth -> auth
               // 允许访问 Knife4j 文档相关路径
               .requestMatchers(
                   "/doc.html",
                   "/v3/api-docs/**",
                   "/swagger-ui/**",
                   "/swagger-ui.html",
                   "/swagger-resources/**",
                   "/webjars/**",
                   "/favicon.ico"
               ).permitAll()
               // 允许前台登录接口
               .requestMatchers("/front/login").permitAll()
               // 允许管理员登录接口
               .requestMatchers("/admin/login").permitAll()
               // 允许下载课表模板接口
               .requestMatchers("/courseSchedule/downloadTemplate").permitAll()
               // 需要认证的接口 - 必须提供有效的JWT Token
               .requestMatchers("/course/**", "/courseSchedule/**", "/teacher/**", "/admin/**").authenticated()
               .anyRequest().permitAll())
           // 禁用CSRF（使用JWT认证，不需要CSRF保护）
           .csrf(csrf -> csrf.disable())
           // 启用CORS（使用CorsConfig中配置的规则）
           .cors(cors -> {})
           // 使用无状态会话（JWT认证不需要Session）
           .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
           // 添加JWT认证过滤器（在UsernamePasswordAuthenticationFilter之前执行）
           .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
       return http.build();
   }

   /**
    * 密码加密器Bean - 使用BCrypt加密算法
    * 数据库中所有密码都使用BCrypt加密存储
    */
   @Bean
   public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder();
   }
}