package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.dto.AttendanceQueryDTO;

import java.time.LocalDate;
import java.util.List;


public interface AttendanceService extends IService<Attendance> {

    Page<Attendance> queryAllAttendanceByCourseId(AttendanceQueryDTO queryDTO);

    Attendance manualAttendance(String courseId);

    Attendance queryCurrentAttendance(String courseId);


    List<Double> queryAttendanceRateByTeacher(String teacherNo);

    List<Double> queryAttendanceRateByClass(String id);
}