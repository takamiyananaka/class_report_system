package com.xuegongbu.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xuegongbu.common.Result;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.Map;

/**
 * 统一认证控制器 - 整合管理员、教师、学院登录
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "统一认证", description = "鉴权接口，支持登录，登出，获取角色,获取用户信息")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 统一登录接口 - 支持管理员、教师、学院登录
     */
    @PostMapping("/login")
    @Operation(summary = "统一登录", description = "支持管理员、教师、学院通过用户名和密码登录系统，返回Token")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("统一登录请求: {}", loginRequest.getUsername());
        LoginResponse response = authService.login(loginRequest);
        if (response == null){
            return Result.error("用户名错误");
        }
        return Result.success(response);
    }

    /**
     * 统一登出接口
     */
    @PostMapping("/logout")
    @Operation(summary = "统一登出", description = "用户退出登录")
    public Result<String> logout() {
        // 获取当前用户信息
        try {
            if (cn.dev33.satoken.stp.StpUtil.isLogin()) {
                Object loginId = cn.dev33.satoken.stp.StpUtil.getLoginId();
                log.info("用户登出: 用户ID={}", loginId);
            }
        } catch (Exception e) {
            log.debug("获取登出用户信息失败", e);
        }
        
        // Sa-Token登出
        cn.dev33.satoken.stp.StpUtil.logout();
        
        log.info("用户登出成功");
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户角色
     */
    @PostMapping("/getRole")
    @Operation(summary = "获取当前用户角色", description = "获取当前用户角色")
    public Result<String> getRole() {
        log.info("获取当前用户角色");
        String role = StpUtil.getSession().get("role").toString();
        if(role == null){
            return Result.error("用户无角色");
        }
        return Result.success(role);
    }

    /**
     * 获取当前用户信息
     */
    @PostMapping("/getUserInfo")
    @Operation(summary = "获取当前用户信息", description = "获取当前用户信息")
    public Result<Map<String, Object>> getUserInfo() {
        log.info("获取当前用户信息");
        Object UserInfo = StpUtil.getSession().get("userInfo");
        Map<String, Object> userInfo = (Map<String, Object>) UserInfo;
        if(userInfo == null){
            return Result.error("用户无信息");
        }
        return Result.success(userInfo);
    }
}