package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.College;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.mapper.CollegeMapper;
import com.xuegongbu.service.CollegeService;
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
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 根据用户名查询学院
        LambdaQueryWrapper<College> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(College::getUsername, loginRequest.getUsername());
        College college = this.getOne(queryWrapper);

        if (college == null) {
            throw new BusinessException("用户名错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), college.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 更新最后登录时间和IP
        college.setLoginTime(LocalDateTime.now());
        // TODO: 从请求中获取IP地址
        this.updateById(college);

        // Sa-Token 登录认证，使用学院ID作为登录标识，并标记角色为college，并存储完整的用户信息
        StpUtil.login(college.getId(), "college");
        StpUtil.getSession().set("userInfo", college);
        String token = StpUtil.getTokenValue();

        // 构造用户信息（不包含密码）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", college.getId());
        userInfo.put("username", college.getUsername());
        userInfo.put("name", college.getName());
        userInfo.put("collegeNo", college.getCollegeNo());
        userInfo.put("role", "college");

        log.info("学院登录成功: {}", college.getUsername());
        return new LoginResponse(token, userInfo);
    }
}
