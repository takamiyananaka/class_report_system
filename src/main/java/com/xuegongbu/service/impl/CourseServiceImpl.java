package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.domain.Course;
import com.xuegongbu.mapper.CourseMapper;
import com.xuegongbu.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Course> queryPage(com.xuegongbu.dto.CourseQueryDTO queryDTO) {
        // 设置分页参数
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Course> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        
        // 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        // 课程ID条件
        if (queryDTO.getCourseId() != null && !queryDTO.getCourseId().trim().isEmpty()) {
            queryWrapper.eq(Course::getCourseId, queryDTO.getCourseId().trim());
        }
        
        // 班级ID条件
        if (queryDTO.getClassId() != null && !queryDTO.getClassId().trim().isEmpty()) {
            queryWrapper.eq(Course::getClassId, queryDTO.getClassId().trim());
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Course::getCreateTime);
        
        log.info("查询课程班级关联，条件：courseId={}, classId={}, pageNum={}, pageSize={}", 
                queryDTO.getCourseId(), queryDTO.getClassId(), pageNum, pageSize);
        
        return this.page(page, queryWrapper);
    }

}
