package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.Course;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;



public interface CourseService extends IService<Course> {

    /**
     * 分页查询课程
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<Course> queryPage(com.xuegongbu.dto.CourseQueryDTO queryDTO);

}
