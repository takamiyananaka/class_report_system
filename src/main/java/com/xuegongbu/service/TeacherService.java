package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
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
     * @param teacher 教师信息
     * @param collegeNo 学院编号（从前端传入）
     * @return 操作结果
     */
    Result<String> addTeacher(Teacher teacher, String collegeNo);
}
