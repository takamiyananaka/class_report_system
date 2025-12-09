package com.xuegongbu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
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
               .anyRequest().permitAll())
           .csrf(csrf -> csrf.disable());
       return http.build();
   }
}