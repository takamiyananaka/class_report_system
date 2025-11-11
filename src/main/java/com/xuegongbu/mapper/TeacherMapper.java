package com.xuegongbu.mapper;

import com.xuegongbu.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TeacherMapper {
    Teacher findByUsername(@Param("username") String username);
    
    Teacher findById(@Param("id") Long id);
    
    int insert(Teacher teacher);
    
    int update(Teacher teacher);
}
