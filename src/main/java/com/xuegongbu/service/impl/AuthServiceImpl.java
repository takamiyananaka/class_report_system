package com.xuegongbu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xuegongbu.common.Constants;
import com.xuegongbu.common.ResultCode;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.domain.Admin;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.security.JwtTokenProvider;
import com.xuegongbu.service.AdminService;
import com.xuegongbu.service.AuthService;
import com.xuegongbu.service.TeacherService;
import com.xuegongbu.utils.RedisUtils;
import com.xuegongbu.vo.AdminVO;
import com.xuegongbu.vo.TeacherVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public LoginResponse adminLogin(LoginRequest request) {
        Admin admin = adminService.findByUsername(request.getUsername());
        if (admin == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        if (Constants.STATUS_DISABLED.equals(admin.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        String token = jwtTokenProvider.generateToken(admin.getId(), admin.getUsername(), Constants.ROLE_ADMIN);
        
        // 缓存用户信息到Redis
        String redisKey = Constants.REDIS_USER_KEY_PREFIX + admin.getId();
        redisUtils.set(redisKey, admin, Constants.REDIS_TOKEN_EXPIRATION, TimeUnit.SECONDS);

        AdminVO adminVO = BeanUtil.copyProperties(admin, AdminVO.class);
        
        return new LoginResponse(token, adminVO);
    }

    @Override
    public LoginResponse teacherLogin(LoginRequest request) {
        Teacher teacher = teacherService.findByUsername(request.getUsername());
        if (teacher == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        if (!passwordEncoder.matches(request.getPassword(), teacher.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        if (Constants.STATUS_DISABLED.equals(teacher.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        String token = jwtTokenProvider.generateToken(teacher.getId(), teacher.getUsername(), Constants.ROLE_TEACHER);
        
        // 缓存用户信息到Redis
        String redisKey = Constants.REDIS_USER_KEY_PREFIX + teacher.getId();
        redisUtils.set(redisKey, teacher, Constants.REDIS_TOKEN_EXPIRATION, TimeUnit.SECONDS);

        TeacherVO teacherVO = BeanUtil.copyProperties(teacher, TeacherVO.class);
        
        return new LoginResponse(token, teacherVO);
    }
}
