package com.xuegongbu.util;

import com.xuegongbu.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 认证信息提取工具类
 * 用于从Spring Security上下文中提取当前登录用户信息
 * 当无法获取用户信息时，返回默认值而不是抛出异常
 */
@Slf4j
public class AuthenticationUtil {

    /**
     * 获取当前登录教师工号（Long类型）
     * 如果无法获取（未登录或解析失败），返回默认值
     * 
     * @return 教师工号，无法获取时返回默认值 Constants.DEFAULT_TEACHER_NO
     */
    public static Long getCurrentTeacherNo() {
        Object principal = getPrincipal();
        if (principal == null) {
            log.warn("未登录或认证信息为空，使用默认教师工号: {}", Constants.DEFAULT_TEACHER_NO);
            return Constants.DEFAULT_TEACHER_NO;
        }

        // 处理不同类型的principal
        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                log.warn("无法将principal转换为Long: {}, 使用默认教师工号: {}", principal, Constants.DEFAULT_TEACHER_NO);
                return Constants.DEFAULT_TEACHER_NO;
            }
        } else {
            log.warn("principal类型不支持: {}, 使用默认教师工号: {}", principal.getClass().getName(), Constants.DEFAULT_TEACHER_NO);
            return Constants.DEFAULT_TEACHER_NO;
        }
    }

    /**
     * 获取当前登录教师工号（String类型）
     * 如果无法获取（未登录或解析失败），返回默认值
     * 
     * @return 教师工号字符串，无法获取时返回默认值 Constants.DEFAULT_TEACHER_NO_STR
     */
    public static String getCurrentTeacherNoStr() {
        Object principal = getPrincipal();
        if (principal == null) {
            log.warn("未登录或认证信息为空，使用默认教师工号: {}", Constants.DEFAULT_TEACHER_NO_STR);
            return Constants.DEFAULT_TEACHER_NO_STR;
        }

        // 处理不同类型的principal
        if (principal instanceof Long) {
            return String.valueOf(principal);
        } else if (principal instanceof String) {
            return (String) principal;
        } else {
            log.warn("principal类型不支持: {}, 使用默认教师工号: {}", principal.getClass().getName(), Constants.DEFAULT_TEACHER_NO_STR);
            return Constants.DEFAULT_TEACHER_NO_STR;
        }
    }

    /**
     * 检查是否已登录（是否可以获取到有效的认证信息）
     * 
     * @return true表示已登录，false表示未登录
     */
    public static boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && 
                   authentication.getPrincipal() != null && 
                   authentication.isAuthenticated();
        } catch (Exception e) {
            log.debug("检查认证状态失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从Spring Security上下文中获取principal
     * 
     * @return principal对象，失败时返回null
     */
    private static Object getPrincipal() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                return null;
            }
            return authentication.getPrincipal();
        } catch (Exception e) {
            log.warn("获取principal失败: {}", e.getMessage());
            return null;
        }
    }
}
