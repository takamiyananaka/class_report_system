package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.mapper.TeacherMapper;
import com.xuegongbu.service.TeacherService;
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
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    // BCrypt密码加密器 - 用于验证数据库中BCrypt加密的密码
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 根据用户名查询教师
        LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teacher::getUsername, loginRequest.getUsername());
        Teacher teacher = this.getOne(queryWrapper);

        if (teacher == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查教师状态
        if (teacher.getStatus() != null && teacher.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 验证密码 - 使用BCrypt验证明文密码与数据库中的加密密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), teacher.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 更新最后登录时间
        teacher.setLastLoginTime(LocalDateTime.now());
        this.updateById(teacher);

        // Sa-Token 登录认证，使用教师工号作为登录标识，并存储完整的用户信息
        StpUtil.login(teacher.getTeacherNo());
        StpUtil.getSession().set("userInfo", teacher);
        String token = StpUtil.getTokenValue();

        // 构造用户信息（不包含密码）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", teacher.getId());
        userInfo.put("username", teacher.getUsername());
        userInfo.put("realName", teacher.getRealName());
        userInfo.put("teacherNo", teacher.getTeacherNo());
        userInfo.put("phone", teacher.getPhone());
        userInfo.put("email", teacher.getEmail());
        userInfo.put("department", teacher.getDepartment());

        log.info("教师登录成功: {}", teacher.getUsername());
        return new LoginResponse(token, userInfo);
    }

}