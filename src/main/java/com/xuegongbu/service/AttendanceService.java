package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.dto.AttendanceQueryDTO;
import com.xuegongbu.dto.AttendanceReportQueryDTO;
import com.xuegongbu.vo.AttendanceVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;


public interface AttendanceService extends IService<Attendance> {

    Page<Attendance> queryAllAttendanceByCourseId(AttendanceQueryDTO queryDTO);

    Attendance manualAttendance(String courseId);

    Attendance queryCurrentAttendance(String courseId);


    List<Double> queryAttendanceRateByTeacher(String teacherNo);

    List<Double> queryAttendanceRateByClass(String id);

    Page<AttendanceVO> queryAttendanceReport(AttendanceReportQueryDTO queryDTO);

    void exportAttendanceReport(AttendanceReportQueryDTO queryDTO, HttpServletResponse response);

}