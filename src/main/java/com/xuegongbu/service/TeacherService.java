package com.xuegongbu.service;

import com.xuegongbu.entity.Teacher;

public interface TeacherService {
    Teacher findByUsername(String username);
    
    Teacher findById(Long id);
}
