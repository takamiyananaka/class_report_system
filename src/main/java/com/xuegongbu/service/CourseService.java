package com.xuegongbu.service;

import com.xuegongbu.domain.Course;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CourseService {
    
    /**
     * 通过Excel导入课程
     * @param file Excel文件
     * @return 导入成功的课程数量
     */
    int importCoursesFromExcel(MultipartFile file) throws IOException;
    
    /**
     * 根据课程ID查询课程
     * @param id 课程ID
     * @return 课程信息
     */
    Course findById(Long id);
    
    /**
     * 查询所有课程
     * @return 课程列表
     */
    List<Course> findAll();
}
