package com.xuegongbu.controller.admin;

import com.xuegongbu.common.Result;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("管理员登录请求：{}", request.getUsername());
        LoginResponse response = authService.adminLogin(request);
        return Result.success(response);
    }
}
