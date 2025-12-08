package com.xuegongbu.service.impl;

import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.mapper.CourseScheduleMapper;
import com.xuegongbu.service.CourseScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseScheduleServiceImpl implements CourseScheduleService {

    @Autowired
    private CourseScheduleMapper courseScheduleMapper;

    @Override
    public CourseSchedule findById(Long id) {
        return courseScheduleMapper.findById(id);
    }

    @Override
    public Map<String, Object> findList(String courseName, Long teacherId, String className,
                                         Integer weekday, String semester, String schoolYear,
                                         Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<CourseSchedule> list = courseScheduleMapper.findList(courseName, teacherId, className, 
                weekday, semester, schoolYear, offset, size);
        int total = courseScheduleMapper.count(courseName, teacherId, className, weekday, semester, schoolYear);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);
        return result;
    }

    @Override
    public List<CourseSchedule> findByTeacherId(Long teacherId) {
        return courseScheduleMapper.findByTeacherId(teacherId);
    }

    @Override
    public List<CourseSchedule> findByClassName(String className) {
        return courseScheduleMapper.findByClassName(className);
    }

    @Override
    public CourseSchedule create(CourseSchedule courseSchedule) {
        courseScheduleMapper.insert(courseSchedule);
        return courseScheduleMapper.findById(courseSchedule.getId());
    }

    @Override
    public CourseSchedule update(Long id, CourseSchedule courseSchedule) {
        courseSchedule.setId(id);
        courseScheduleMapper.update(courseSchedule);
        return courseScheduleMapper.findById(id);
    }

    @Override
    public boolean delete(Long id) {
        return courseScheduleMapper.deleteById(id) > 0;
    }
}
