package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Alert;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.CourseSchedule;

import java.util.List;

public interface AlertService extends IService<Alert> {
    List<Alert> listByTeacherId(Long teacherId);
    
    /**
     * 检查考勤记录并生成相应的预警
     * @param attendance 考勤记录
     * @param course 课程安排
     */
    void checkAndGenerateAlert(Attendance attendance, CourseSchedule course);
}