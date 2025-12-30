package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


public interface TeacherService extends IService<Teacher> {

    /**
     * 教师登录
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 分页查询教师
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<Teacher> queryPage(com.xuegongbu.dto.TeacherQueryDTO queryDTO);

    Result<String> importTeachers(MultipartFile file, String collegeNo);
}
