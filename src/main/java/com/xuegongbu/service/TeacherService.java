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

    /**
     * 批量导入教师
     */
    Result<String> importTeachers(MultipartFile file, String collegeNo);
    
    /**
     * 添加单个教师
     * @param teacherNo 教师工号
     * @param realName 真实姓名
     * @param collegeNo 学院号
     * @param departmentName 部门名称
     * @return 添加结果
     */
    Result<Teacher> addSingleTeacher(String teacherNo, String realName, String collegeNo, String departmentName);
}
