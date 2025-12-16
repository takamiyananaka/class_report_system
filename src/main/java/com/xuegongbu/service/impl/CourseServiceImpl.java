package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.Course;
import com.xuegongbu.mapper.CourseMapper;
import com.xuegongbu.service.CourseService;
import com.xuegongbu.service.DeviceService;
import com.xuegongbu.util.CountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        
        // 教师工号条件
        if (queryDTO.getTeacherNo() != null && !queryDTO.getTeacherNo().trim().isEmpty()) {
            queryWrapper.eq(Course::getTeacherNo, queryDTO.getTeacherNo().trim());
        }
        
        // 班级名称条件（模糊查询）
        if (queryDTO.getClassName() != null && !queryDTO.getClassName().trim().isEmpty()) {
            queryWrapper.like(Course::getClassName, queryDTO.getClassName().trim());
        }
        
        // 课程名称条件（模糊查询）
        if (queryDTO.getCourseName() != null && !queryDTO.getCourseName().trim().isEmpty()) {
            queryWrapper.like(Course::getCourseName, queryDTO.getCourseName().trim());
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Course::getCreateTime);
        
        log.info("查询课程，条件：teacherNo={}, className={}, courseName={}, pageNum={}, pageSize={}", 
                queryDTO.getTeacherNo(), queryDTO.getClassName(), queryDTO.getCourseName(), pageNum, pageSize);
        
        return this.page(page, queryWrapper);
    }

}
