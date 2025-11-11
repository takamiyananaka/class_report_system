package com.xuegongbu.service.impl;

import com.xuegongbu.entity.Teacher;
import com.xuegongbu.mapper.TeacherMapper;
import com.xuegongbu.service.TeacherService;
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
}
