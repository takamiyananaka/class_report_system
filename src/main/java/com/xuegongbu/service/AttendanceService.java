package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Attendance;

import java.util.List;

public interface AttendanceService extends IService<Attendance> {
    List<Attendance> queryAllAttendanceByCourseId(String courseId);

    void manualAttendance(String courseId);

    Attendance queryCurrentAttendance(String courseId);
}
