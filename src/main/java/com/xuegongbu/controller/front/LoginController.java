package com.xuegongbu.controller.front;

import com.xuegongbu.common.Result;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/front")
@Tag(name = "登录管理", description = "登录相关接口")
public class LoginController {

    @Autowired
    private TeacherService teacherService;

    @PostMapping("/login")
    @Operation(summary = "教师登录", description = "教师通过用户名和密码登录系统，返回JWT token")
    public Result<LoginResponse> login(@RequestBody(description = "登录请求") @Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest loginRequest) {
        log.info("教师登录请求: {}", loginRequest.getUsername());
        LoginResponse response = teacherService.login(loginRequest);
        return Result.success(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "教师登出", description = "教师退出登录，清除认证上下文")
    public Result<String> logout() {
        // 获取当前用户信息（只记录非敏感信息）
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof Long) {
                    log.info("教师登出: 用户ID={}", principal);
                } else {
                    log.info("教师登出: 用户类型={}", principal.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            log.debug("获取登出用户信息失败", e);
        }
        
        // 清除Spring Security上下文
        SecurityContextHolder.clearContext();
        
        log.info("教师登出成功");
        return Result.success("登出成功");
    }

}