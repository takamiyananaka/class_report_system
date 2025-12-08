package com.xuegongbu.service;

import com.xuegongbu.domain.Teacher;

public interface TeacherService {
    Teacher findByUsername(String username);
    
    Teacher findById(Long id);
    
    /**
     * 创建教师
     */
    Teacher create(Teacher teacher);
    
    /**
     * 更新教师
     */
    Teacher update(Long id, Teacher teacher);
    
    /**
     * 验证教师数据（包括辅导员身份的部门格式验证）
     * @param teacher 教师数据
     * @return 验证错误信息，null表示验证通过
     */
    String validateTeacher(Teacher teacher);
}
