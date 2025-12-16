package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.dto.AttendanceQueryDTO;

import java.util.List;

public interface AttendanceService extends IService<Attendance> {
    @Deprecated
    List<Attendance> queryAllAttendanceByCourseId(String courseId);
    
    Page<Attendance> queryAllAttendanceByCourseId(AttendanceQueryDTO queryDTO);

    Attendance manualAttendance(String courseId);

    Attendance queryCurrentAttendance(String courseId);
}