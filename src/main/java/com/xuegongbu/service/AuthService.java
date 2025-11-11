package com.xuegongbu.service;

import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;

public interface AuthService {
    LoginResponse adminLogin(LoginRequest request);
    
    LoginResponse teacherLogin(LoginRequest request);
}
