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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.dev33.satoken.stp.StpUtil;

@Slf4j
@RestController
@RequestMapping(value = "/front")
@Tag(name = "登录管理", description = "登录相关接口")
public class LoginController {

    @Autowired
    private TeacherService teacherService;

    @PostMapping("/login")
    @Operation(summary = "教师登录", description = "教师通过用户名和密码登录系统，返回Token")
    public Result<LoginResponse> login(@RequestBody(description = "登录请求") @Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest loginRequest) {
        log.info("教师登录请求: {}", loginRequest.getUsername());
        LoginResponse response = teacherService.login(loginRequest);
        return Result.success(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "教师登出", description = "教师退出登录")
    public Result<String> logout() {
        // 获取当前用户信息
        try {
            if (StpUtil.isLogin()) {
                Object loginId = StpUtil.getLoginId();
                log.info("教师登出: 用户ID={}", loginId);
            }
        } catch (Exception e) {
            log.debug("获取登出用户信息失败", e);
        }
        
        // Sa-Token登出
        StpUtil.logout();
        
        log.info("教师登出成功");
        return Result.success("登出成功");
    }

}