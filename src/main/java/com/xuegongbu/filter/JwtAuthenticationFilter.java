package com.xuegongbu.filter;

import com.xuegongbu.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null) {
            log.debug("JWT认证过滤器 - 未找到Authorization Header，请求路径: {}", requestPath);
        } else if (!authHeader.startsWith("Bearer ")) {
            log.warn("JWT认证过滤器 - Authorization Header格式错误，应以'Bearer '开头，实际值前缀: {}, 请求路径: {}", 
                    authHeader.substring(0, Math.min(20, authHeader.length())), requestPath);
        } else {
            String token = authHeader.substring(7);
            log.debug("JWT认证过滤器 - 处理Token，长度: {}, 请求路径: {}", token.length(), requestPath);
            
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                String teacherNo = jwtUtil.getTeacherNoFromToken(token);
                
                if (userId != null && username != null) {
                    // 使用teacherNo作为principal（如果是管理员则teacherNo为null，使用userId）
                    Object principal = teacherNo != null ? teacherNo : userId;
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("JWT认证成功 - 用户ID: {}, 用户名: {}, 教师工号: {}, 请求路径: {}", userId, username, teacherNo, requestPath);
                } else {
                    log.warn("JWT认证失败 - 无法从Token中提取用户信息，用户ID: {}, 用户名: {}, 教师工号: {}, 请求路径: {}", 
                            userId, username, teacherNo, requestPath);
                }
            } else {
                log.warn("JWT认证失败 - Token验证失败，请求路径: {}", requestPath);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
