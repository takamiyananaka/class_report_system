package com.xuegongbu.service.impl;

import com.xuegongbu.domain.Teacher;
import com.xuegongbu.mapper.TeacherMapper;
import com.xuegongbu.service.TeacherService;
import com.xuegongbu.utils.TeacherValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    public Teacher findByUsername(String username) {
        return teacherMapper.findByUsername(username);
    }

    @Override
    public Teacher findById(Long id) {
        return teacherMapper.findById(id);
    }

    @Override
    public Teacher create(Teacher teacher) {
        teacherMapper.insert(teacher);
        return teacherMapper.findById(teacher.getId());
    }

    @Override
    public Teacher update(Long id, Teacher teacher) {
        teacher.setId(id);
        teacherMapper.update(teacher);
        return teacherMapper.findById(id);
    }

    @Override
    public String validateTeacher(Teacher teacher) {
        // 验证身份字段
        String identityError = TeacherValidationUtils.validateIdentity(teacher.getIdentity());
        if (identityError != null) {
            return identityError;
        }
        
        // 验证辅导员身份时的部门格式
        String departmentError = TeacherValidationUtils.validateDepartmentForCounselor(
                teacher.getIdentity(), teacher.getDepartment());
        if (departmentError != null) {
            return departmentError;
        }
        
        return null;
    }
}
