package com.xuegongbu.controller.front;

import com.xuegongbu.common.Result;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.service.TeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/front")
@Api(tags = "登录管理")
public class LoginController {

    @Autowired
    private TeacherService teacherService;

    @PostMapping("/login")
    @ApiOperation(value = "教师登录", notes = "教师通过用户名和密码登录系统，返回JWT token")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("教师登录请求: {}", loginRequest.getUsername());
        LoginResponse response = teacherService.login(loginRequest);
        return Result.success(response);
    }

}
