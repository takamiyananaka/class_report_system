package com.xuegongbu.service;

import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;

/**
 * 统一认证服务接口
 */
public interface AuthService {
    
    /**
     * 统一登录方法 - 根据用户名自动识别用户类型并进行登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 发送忘记密码验证码
     * @param email 邮箱地址
     * @return 操作结果
     */
    String sendForgotPasswordCode(String email);

    /**
     * 重置密码
     * @param email 邮箱地址
     * @param verificationCode 验证码
     * @param newPassword 新密码
     * @return 操作结果
     */
    String resetPassword(String email, String verificationCode, String newPassword);
}