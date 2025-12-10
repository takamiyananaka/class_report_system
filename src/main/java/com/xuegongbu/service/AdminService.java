package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Admin;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import org.springframework.stereotype.Service;


public interface AdminService extends IService<Admin> {

    /**
     * 管理员登录
     */
    LoginResponse login(LoginRequest loginRequest);

}
