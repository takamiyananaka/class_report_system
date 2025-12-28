package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.College;
import com.xuegongbu.domain.CollegeAdmin;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.mapper.CollegeAdminMapper;
import com.xuegongbu.mapper.CollegeMapper;
import com.xuegongbu.service.CollegeAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import cn.dev33.satoken.stp.StpUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 学院管理员服务实现类
 */
@Slf4j
@Service
public class CollegeAdminServiceImpl extends ServiceImpl<CollegeAdminMapper, CollegeAdmin> implements CollegeAdminService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CollegeMapper collegeMapper;
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 根据用户名查询学院管理员
        LambdaQueryWrapper<CollegeAdmin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CollegeAdmin::getUsername, loginRequest.getUsername());
        CollegeAdmin collegeAdmin = this.getOne(queryWrapper);

        if (collegeAdmin == null) {
            throw new BusinessException("用户名错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), collegeAdmin.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 检查管理员状态
        if (collegeAdmin.getStatus() != null && collegeAdmin.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系系统管理员");
        }

        // 更新最后登录时间
        collegeAdmin.setLastLoginTime(LocalDateTime.now());
        collegeAdmin.setLastLoginIp("0.0.0.0"); // 这里应该从请求中获取真实IP
        this.updateById(collegeAdmin);

        College collegeInfo = collegeMapper.selectById(collegeAdmin.getCollegeId());
        // Sa-Token 登录认证，使用学院管理员ID作为登录标识，并标记角色为college_admin，并存储完整的用户信息
        StpUtil.login(collegeAdmin.getId());
        StpUtil.getSession().set("role", "college_admin");
        StpUtil.getSession().set("userInfo", collegeAdmin);
        StpUtil.getSession().set("collegeInfo", collegeInfo);
        String token = StpUtil.getTokenValue();

        // 构造用户信息（不包含密码）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", collegeAdmin.getId());
        userInfo.put("username", collegeAdmin.getUsername());
        userInfo.put("realName", collegeAdmin.getRealName());
        userInfo.put("phone", collegeAdmin.getPhone());
        userInfo.put("email", collegeAdmin.getEmail());
        userInfo.put("collegeId", collegeAdmin.getCollegeId());
        if (collegeInfo != null) {
            userInfo.put("collegeName", collegeInfo.getName());
            userInfo.put("collegeNo", collegeInfo.getCollegeNo());
        }
        userInfo.put("role", "college_admin");

        log.info("学院管理员登录成功: {}", collegeAdmin.getUsername());
        return new LoginResponse(token, userInfo);
    }
}