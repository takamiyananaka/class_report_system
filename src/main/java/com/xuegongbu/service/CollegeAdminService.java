package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.CollegeAdmin;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;

/**
 * 学院管理员服务接口
 */
public interface CollegeAdminService extends IService<CollegeAdmin> {
    
    /**
     * 学院管理员登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);
}