package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Course;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CourseService extends IService<Course> {

    /**
     * Import courses from Excel file
     * @param file Excel file
     * @return number of imported courses
     * @throws IOException if file reading fails
     */
    int importCoursesFromExcel(MultipartFile file) throws IOException;

    /**
     * Find course by id
     * @param id course id
     * @return course
     */
    Course findById(Long id);

    /**
     * Find all courses
     * @return list of courses
     */
    List<Course> findAll();
}
