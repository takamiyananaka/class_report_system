package com.xuegongbu.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xuegongbu.common.Result;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.dto.ForgotPasswordRequest;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            if (StpUtil.isLogin()) {
                Object loginId =StpUtil.getLoginId();
                log.info("用户登出: 用户ID={}", loginId);
            }
        } catch (Exception e) {
            log.debug("获取登出用户信息失败", e);
        }
        
        // Sa-Token登出
        StpUtil.logout();
        
        log.info("用户登出成功");
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户角色
     */
    @GetMapping("/getRole")
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
    @GetMapping("/getUserInfo")
    @Operation(summary = "获取当前用户信息", description = "获取当前用户信息")
    public Result<Object> getUserInfo() {
        log.info("获取当前用户信息");
        Object userInfo = StpUtil.getSession().get("userInfo");
        return Result.success(userInfo);
    }
    
    /**
     * 发送忘记密码验证码
     */
    @PostMapping("/forgot-password/send-code")
    @Operation(summary = "发送忘记密码验证码", description = "通过邮箱发送验证码用于密码重置")
    public Result<String> sendForgotPasswordCode(@RequestBody @Valid ForgotPasswordRequest request) {
        log.info("发送忘记密码验证码请求: {}", request.getEmail());
        try {
            String result = authService.sendForgotPasswordCode(request.getEmail());
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("发送忘记密码验证码失败", e);
            return Result.error("发送验证码失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/forgot-password/reset")
    @Operation(summary = "重置密码", description = "通过邮箱和验证码重置密码")
    public Result<String> resetPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        log.info("重置密码请求: {}", request.getEmail());
        try {
            String result = authService.resetPassword(request.getEmail(), request.getVerificationCode(), request.getNewPassword());
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.error("重置密码失败: " + e.getMessage());
        }
    }
}