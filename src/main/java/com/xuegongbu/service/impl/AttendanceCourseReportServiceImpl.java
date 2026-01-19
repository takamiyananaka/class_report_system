package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.AttendanceCourseReport;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.mapper.AttendanceCourseReportMapper;
import com.xuegongbu.mapper.CourseScheduleMapper;
import com.xuegongbu.service.AttendanceCourseReportService;
import com.xuegongbu.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程考勤报表 Service 实现类
 */
@Service
public class AttendanceCourseReportServiceImpl extends ServiceImpl<AttendanceCourseReportMapper, AttendanceCourseReport> implements AttendanceCourseReportService {
    private final CourseService courseService;
    
    @Autowired
    private CourseScheduleMapper courseScheduleMapper;

    public AttendanceCourseReportServiceImpl(CourseService courseService) {
        this.courseService = courseService;
    }

    @Override
    public void updateReport(Attendance attendance, CourseSchedule course) {
        //更新课程报表
        LambdaQueryWrapper<AttendanceCourseReport> attendanceCourseReportLambdaQueryWrapper = new LambdaQueryWrapper<>();
        attendanceCourseReportLambdaQueryWrapper.eq(AttendanceCourseReport::getOrderNo, course.getOrderNo());
        AttendanceCourseReport attendanceCourseReport = this.getOne(attendanceCourseReportLambdaQueryWrapper);
        if (attendanceCourseReport == null) {
            attendanceCourseReport = new AttendanceCourseReport();
            attendanceCourseReport.setOrderNo(course.getOrderNo());
            attendanceCourseReport.setReportDate(attendance.getCheckTime().toLocalDate());
            attendanceCourseReport.setAttendanceRecordCount(1);
            attendanceCourseReport.setTotalExpectedCount(course.getExpectedCount());
            attendanceCourseReport.setTotalActualCount(attendance.getActualCount());
            attendanceCourseReport.setAverageAttendanceRate(attendance.getAttendanceRate());
            attendanceCourseReport.setSemesterName(course.getSemesterName()); // 设置学期名称
            this.save(attendanceCourseReport);
        } else {
            attendanceCourseReport.setAttendanceRecordCount(attendanceCourseReport.getAttendanceRecordCount() + 1);
            attendanceCourseReport.setTotalExpectedCount(attendanceCourseReport.getTotalExpectedCount() + course.getExpectedCount());
            attendanceCourseReport.setTotalActualCount(attendanceCourseReport.getTotalActualCount() + attendance.getActualCount());
            // 防止除零错误：当总期望数量为0时，出勤率设为0
            if (attendanceCourseReport.getTotalExpectedCount() != 0) {
                // 将整数转换为BigDecimal进行除法运算
                BigDecimal totalActual = new BigDecimal(attendanceCourseReport.getTotalActualCount());
                BigDecimal totalExpected = new BigDecimal(attendanceCourseReport.getTotalExpectedCount());
                attendanceCourseReport.setAverageAttendanceRate(totalActual.divide(totalExpected, 2, BigDecimal.ROUND_HALF_UP));
            } else {
                attendanceCourseReport.setAverageAttendanceRate(BigDecimal.ZERO);
            }
            attendanceCourseReport.setSemesterName(course.getSemesterName()); // 更新学期名称
        }
    }

    @Override
    public List<AttendanceCourseReport> getReportsByOrderNoAndType(List<String> orderNo, int periodType) {
        LambdaQueryWrapper<AttendanceCourseReport> queryWrapper = new LambdaQueryWrapper<>();

        // 根据课序号过滤
        queryWrapper.in(AttendanceCourseReport::getOrderNo, orderNo);

        // 根据查询类型设置日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate; // 默认为当天

        switch (periodType) {
            case 1: // 按日
                queryWrapper.eq(AttendanceCourseReport::getReportDate, startDate);
                break;
            case 2: // 按周：往前推6天，总共7天
                startDate = endDate.minusDays(6);
                queryWrapper.between(AttendanceCourseReport::getReportDate, startDate, endDate);
                break;
            case 3: // 按月：往前推29天，总共30天
                startDate = endDate.minusDays(29);
                queryWrapper.between(AttendanceCourseReport::getReportDate, startDate, endDate);
                break;
            default:
                // 如果没有指定查询类型，默认查询当天
                queryWrapper.eq(AttendanceCourseReport::getReportDate, startDate);
                break;
        }

        // 按日期升序排列
        queryWrapper.orderByAsc(AttendanceCourseReport::getReportDate);

        return this.list(queryWrapper);
    }

}