package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.College;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;

/**
 * 学院服务接口
 */
public interface CollegeService extends IService<College> {
    
    /**
     * 学院登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);
}
