package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.Course;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;



public interface CourseService extends IService<Course> {


    List<Attendance> queryAttendanceByCourseId(Long courseId);

    void manualAttendance(Long courseId);
}
