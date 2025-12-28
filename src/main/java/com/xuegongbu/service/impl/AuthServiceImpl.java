package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuegongbu.common.Result;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.Admin;
import com.xuegongbu.domain.College;
import com.xuegongbu.domain.CollegeAdmin;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.mapper.AdminMapper;
import com.xuegongbu.mapper.CollegeAdminMapper;
import com.xuegongbu.mapper.CollegeMapper;
import com.xuegongbu.mapper.TeacherMapper;
import com.xuegongbu.service.AuthService;
import com.xuegongbu.service.AdminService;
import com.xuegongbu.service.TeacherService;
import com.xuegongbu.service.CollegeService;
import com.xuegongbu.service.CollegeAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
   @Autowired
   private CollegeAdminMapper collegeAdminMapper;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private CollegeAdminService collegeAdminService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 首先尝试在管理员表中查找用户
        LambdaQueryWrapper<Admin> adminQueryWrapper = new LambdaQueryWrapper<>();
        adminQueryWrapper.eq(Admin::getUsername, loginRequest.getUsername());
        Admin admin = adminMapper.selectOne(adminQueryWrapper);

        if (admin != null) {
            // 找到管理员，进行管理员登录验证
            return adminService.login(loginRequest);
        }

        // 管理员表中未找到，尝试在教师表中查找
        LambdaQueryWrapper<Teacher> teacherQueryWrapper = new LambdaQueryWrapper<>();
        teacherQueryWrapper.eq(Teacher::getUsername, loginRequest.getUsername());
        Teacher teacher = teacherMapper.selectOne(teacherQueryWrapper);

        if (teacher != null) {
            // 找到教师，进行教师登录验证
            return teacherService.login(loginRequest);
        }

        // 教师表中未找到，尝试在学院管理员表中查找
        LambdaQueryWrapper<CollegeAdmin> collegeAdminQueryWrapper = new LambdaQueryWrapper<>();
        collegeAdminQueryWrapper.eq(CollegeAdmin::getUsername, loginRequest.getUsername());
        CollegeAdmin collegeAdmin = collegeAdminMapper.selectOne(collegeAdminQueryWrapper);
        if (collegeAdmin != null) {
            return collegeAdminService.login(loginRequest);
        }
            
        
        return null;
    }
}