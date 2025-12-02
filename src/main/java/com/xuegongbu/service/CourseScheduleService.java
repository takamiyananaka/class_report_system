package com.xuegongbu.service;

import com.xuegongbu.entity.CourseSchedule;

import java.util.List;
import java.util.Map;

public interface CourseScheduleService {
    
    /**
     * 根据ID查询课表
     */
    CourseSchedule findById(Long id);
    
    /**
     * 查询课表列表（分页）
     */
    Map<String, Object> findList(String courseName, Long teacherId, String className, 
                                  Integer weekday, String semester, String schoolYear,
                                  Integer page, Integer size);
    
    /**
     * 根据教师ID查询课表
     */
    List<CourseSchedule> findByTeacherId(Long teacherId);
    
    /**
     * 根据班级名称查询课表
     */
    List<CourseSchedule> findByClassName(String className);
    
    /**
     * 创建课表
     */
    CourseSchedule create(CourseSchedule courseSchedule);
    
    /**
     * 更新课表
     */
    CourseSchedule update(Long id, CourseSchedule courseSchedule);
    
    /**
     * 删除课表
     */
    boolean delete(Long id);
}
