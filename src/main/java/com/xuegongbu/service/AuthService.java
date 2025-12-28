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
}