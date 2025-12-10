package com.xuegongbu.controller.front;

import com.xuegongbu.common.Result;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.service.TeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/front")
@Api(tags = "登录管理")
public class LoginController {

    @Autowired
    private TeacherService teacherService;

    @PostMapping("/login")
    @ApiOperation(value = "教师登录", notes = "教师通过用户名和密码登录系统，返回JWT token")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("教师登录请求: {}", loginRequest.getUsername());
        LoginResponse response = teacherService.login(loginRequest);
        return Result.success(response);
    }

    @PostMapping("/logout")
    @ApiOperation(value = "教师登出", notes = "教师退出登录，清除认证上下文")
    public Result<String> logout() {
        // 获取当前用户信息
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        
        if (principal != null) {
            log.info("教师登出: 用户ID={}", principal);
        }
        
        // 清除Spring Security上下文
        SecurityContextHolder.clearContext();
        
        log.info("教师登出成功");
        return Result.success("登出成功");
    }

}
