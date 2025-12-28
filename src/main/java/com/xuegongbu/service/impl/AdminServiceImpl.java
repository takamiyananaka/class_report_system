package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.Admin;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.mapper.AdminMapper;
import com.xuegongbu.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import cn.dev33.satoken.stp.StpUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 根据用户名查询管理员
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getUsername, loginRequest.getUsername());
        Admin admin = this.getOne(queryWrapper);

        if (admin == null) {
            throw new BusinessException("用户名错误");
        }

        // 检查管理员状态
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系系统管理员");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), admin.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 更新最后登录时间
        admin.setLastLoginTime(LocalDateTime.now());
        this.updateById(admin);

        // Sa-Token 登录认证，使用管理员ID作为登录标识，并标记角色为admin，并存储完整的用户信息
        StpUtil.login(admin.getId());
        StpUtil.getSession().set("role", "admin");
        StpUtil.getSession().set("userInfo", admin);
        String token = StpUtil.getTokenValue();

        // 构造用户信息（不包含密码）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", admin.getId());
        userInfo.put("username", admin.getUsername());
        userInfo.put("realName", admin.getRealName());
        userInfo.put("phone", admin.getPhone());
        userInfo.put("email", admin.getEmail());
        userInfo.put("role", "admin");

        log.info("管理员登录成功: {}", admin.getUsername());
        return new LoginResponse(token, userInfo);
    }

}