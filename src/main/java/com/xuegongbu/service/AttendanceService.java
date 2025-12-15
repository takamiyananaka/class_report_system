package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Attendance;

import java.util.List;

public interface AttendanceService extends IService<Attendance> {
    List<Attendance> queryAllAttendanceByCourseId(Long courseId);

    Attendance manualAttendance(Long courseId);

    Attendance queryCurrentAttendance(Long courseId);
}