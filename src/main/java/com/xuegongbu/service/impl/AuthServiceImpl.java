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
import com.xuegongbu.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private MailService mailService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Override
    public String sendForgotPasswordCode(String email) {
        // 验证邮箱是否存在于数据库中
        if (!emailExistsInAnyTable(email)) {
            throw new BusinessException("该邮箱不存在于系统中");
        }

        // 生成6位随机验证码
        String verificationCode = generateVerificationCode();
        
        // 将验证码存入Redis，设置5分钟过期时间
        redisTemplate.opsForValue().set("forgot_password_code:" + email, verificationCode, 5, TimeUnit.MINUTES);
        
        // 发送验证码邮件
        mailService.sendVerificationCode(email, verificationCode);
        
        return "验证码已发送到您的邮箱，请注意查收";
    }

    @Override
    public String resetPassword(String email, String verificationCode, String newPassword) {
        // 验证邮箱是否存在于数据库中
        if (!emailExistsInAnyTable(email)) {
            throw new BusinessException("该邮箱不存在于系统中");
        }

        // 验证验证码是否正确
        String storedCode = redisTemplate.opsForValue().get("forgot_password_code:" + email);
        if (storedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!storedCode.equals(verificationCode)) {
            throw new BusinessException("验证码错误");
        }

        // 根据邮箱找到用户并更新密码
        updatePasswordByEmail(email, newPassword);

        // 删除Redis中的验证码
        redisTemplate.delete("forgot_password_code:" + email);

        return "密码重置成功";
    }

    /**
     * 验证邮箱是否存在于任何用户表中
     */
    private boolean emailExistsInAnyTable(String email) {
        // 检查管理员表
        LambdaQueryWrapper<Admin> adminQuery = new LambdaQueryWrapper<>();
        adminQuery.eq(Admin::getEmail, email);
        if (adminMapper.selectCount(adminQuery) > 0) {
            return true;
        }

        // 检查教师表
        LambdaQueryWrapper<Teacher> teacherQuery = new LambdaQueryWrapper<>();
        teacherQuery.eq(Teacher::getEmail, email);
        if (teacherMapper.selectCount(teacherQuery) > 0) {
            return true;
        }

        // 检查学院管理员表
        LambdaQueryWrapper<CollegeAdmin> collegeAdminQuery = new LambdaQueryWrapper<>();
        collegeAdminQuery.eq(CollegeAdmin::getEmail, email);
        return collegeAdminMapper.selectCount(collegeAdminQuery) > 0;
    }

    /**
     * 根据邮箱更新用户密码
     */
    private void updatePasswordByEmail(String email, String newPassword) {
        // 检查管理员表
        LambdaQueryWrapper<Admin> adminQuery = new LambdaQueryWrapper<>();
        adminQuery.eq(Admin::getEmail, email);
        Admin admin = adminMapper.selectOne(adminQuery);
        if (admin != null) {
            admin.setPassword(passwordEncoder.encode(newPassword));
            adminMapper.updateById(admin);
            return;
        }

        // 检查教师表
        LambdaQueryWrapper<Teacher> teacherQuery = new LambdaQueryWrapper<>();
        teacherQuery.eq(Teacher::getEmail, email);
        Teacher teacher = teacherMapper.selectOne(teacherQuery);
        if (teacher != null) {
            teacher.setPassword(passwordEncoder.encode(newPassword));
            teacherMapper.updateById(teacher);
            return;
        }

        // 检查学院管理员表
        LambdaQueryWrapper<CollegeAdmin> collegeAdminQuery = new LambdaQueryWrapper<>();
        collegeAdminQuery.eq(CollegeAdmin::getEmail, email);
        CollegeAdmin collegeAdmin = collegeAdminMapper.selectOne(collegeAdminQuery);
        if (collegeAdmin != null) {
            collegeAdmin.setPassword(passwordEncoder.encode(newPassword));
            collegeAdminMapper.updateById(collegeAdmin);
        }
    }

    /**
     * 生成6位随机验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}