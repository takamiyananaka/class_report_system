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
    @Autowired
    private DeviceService deviceService;
    @Override
    public List<Attendance> queryAttendanceByCourseId(Long courseId) {
        //获取到课程信息
        Course course = this.getById(courseId);
        if(course == null){
            throw new BusinessException("无效的id");
        }

        return List.of();
    }

    @Override
    public void manualAttendance(Long courseId) {
        //获取到课程信息
        Course course = this.getById(courseId);
        if(course == null){
            throw new BusinessException("无效的id");
        }
        //检查当前是否在上课时间内
        if(course.getCourseDate().isBefore(LocalDate.now()) || course.getCourseDate().isAfter(LocalDate.now())){
            throw new BusinessException("不在上课时间");
        }

        String className = course.getClassName();
        Map<String, String> deviceUrls = deviceService.getDeviceUrl(className);
        //调用模型获取当前教室人数
        int count = CountUtil.getCount(deviceUrls);
    }
}
