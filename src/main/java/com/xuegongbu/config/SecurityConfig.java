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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

   @Autowired
   private JwtAuthenticationFilter jwtAuthenticationFilter;

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
                   "/webjars/**"
               ).permitAll()
               // 允许前台登录接口
               .requestMatchers("/front/login").permitAll()
               // 允许管理员登录接口
               .requestMatchers("/admin/login").permitAll()
               // 允许下载课表模板接口
               . requestMatchers("/courseSchedule/downloadTemplate").permitAll()
               // 需要认证的接口
               .requestMatchers("/course/**", "/courseSchedule/**", "/teacher/**", "/admin/**").authenticated()
               .anyRequest().permitAll())
           .csrf(csrf -> csrf.disable())
           .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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