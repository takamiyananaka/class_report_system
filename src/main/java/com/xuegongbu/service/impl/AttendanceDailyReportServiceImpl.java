package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.AttendanceDailyReport;
import com.xuegongbu.domain.Class;
import com.xuegongbu.domain.Course;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.mapper.AttendanceDailyReportMapper;
import com.xuegongbu.mapper.ClassMapper;
import com.xuegongbu.mapper.CollegeMapper;
import com.xuegongbu.mapper.TeacherMapper;
import com.xuegongbu.service.AttendanceDailyReportService;
import com.xuegongbu.service.ClassService;
import com.xuegongbu.service.CollegeService;
import com.xuegongbu.service.CourseService;
import com.xuegongbu.vo.AttendanceChartVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 班级每日考勤报表 Service 实现类
 */
@Service
@Slf4j
public class AttendanceDailyReportServiceImpl extends ServiceImpl<AttendanceDailyReportMapper, AttendanceDailyReport> implements AttendanceDailyReportService {
    private final CourseService courseService;
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private CollegeService collegeService;
    
    @Autowired
    private ClassMapper classMapper;
    
    @Autowired
    private CollegeMapper collegeMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;

    public AttendanceDailyReportServiceImpl(CourseService courseService) {
        this.courseService = courseService;
    }

    @Override
    public void updateReport(Attendance attendance, CourseSchedule course) {
        //更新班级日报表
        LambdaQueryWrapper<Course> courseLambdaQueryWrapper = new LambdaQueryWrapper<>();
        courseLambdaQueryWrapper.eq(Course::getCourseId, course.getId());
        List< Course> courses = courseService.list(courseLambdaQueryWrapper);
        List<String> classIds = courses.stream()
                .map(Course::getClassId)
                .collect(Collectors.toList());
        for (String classId : classIds) {
            LambdaQueryWrapper<AttendanceDailyReport> attendanceDailyReportLambdaQueryWrapper = new LambdaQueryWrapper<>();
            attendanceDailyReportLambdaQueryWrapper.eq(AttendanceDailyReport::getClassId, classId);
            attendanceDailyReportLambdaQueryWrapper.eq(AttendanceDailyReport::getReportDate, attendance.getCheckTime().toLocalDate());
            AttendanceDailyReport attendanceDailyReport = this.getOne(attendanceDailyReportLambdaQueryWrapper);
            if (attendanceDailyReport == null) { 
                attendanceDailyReport = new AttendanceDailyReport();
                attendanceDailyReport.setClassId(classId);
                attendanceDailyReport.setReportDate(attendance.getCheckTime().toLocalDate());
                attendanceDailyReport.setAttendanceRecordCount(1);
                attendanceDailyReport.setTotalExpectedCount(course.getExpectedCount());
                attendanceDailyReport.setTotalActualCount(attendance.getActualCount());
                attendanceDailyReport.setAverageAttendanceRate(attendance.getAttendanceRate());
                attendanceDailyReport.setSemesterName(course.getSemesterName()); // 设置学期名称
                this.save(attendanceDailyReport);
                log.info("新增班级每日考勤报表：{}", attendanceDailyReport);
            }else {
                attendanceDailyReport.setAttendanceRecordCount(attendanceDailyReport.getAttendanceRecordCount() + 1);
                attendanceDailyReport.setTotalExpectedCount(attendanceDailyReport.getTotalExpectedCount() + course.getExpectedCount());
                attendanceDailyReport.setTotalActualCount(attendanceDailyReport.getTotalActualCount() + attendance.getActualCount());
                // 防止除零错误：当总期望数量为0时，出勤率设为0
                if (attendanceDailyReport.getTotalExpectedCount() != 0) {
                    // 将整数转换为BigDecimal进行除法运算
                    BigDecimal totalActual = new BigDecimal(attendanceDailyReport.getTotalActualCount());
                    BigDecimal totalExpected = new BigDecimal(attendanceDailyReport.getTotalExpectedCount());
                    attendanceDailyReport.setAverageAttendanceRate(totalActual.divide(totalExpected, 2, BigDecimal.ROUND_HALF_UP));
                } else {
                    attendanceDailyReport.setAverageAttendanceRate(BigDecimal.ZERO);
                }
                attendanceDailyReport.setSemesterName(course.getSemesterName()); // 更新学期名称
                this.update(attendanceDailyReport, attendanceDailyReportLambdaQueryWrapper);
                log.info("更新班级每日考勤报表：{}", attendanceDailyReport);
            }
        }
    }
    


    @Override
    public List<AttendanceDailyReport> getAttendanceChartByClassIdAndType(List<String> classIds, Integer granularity, String semesterName) {
        LambdaQueryWrapper<AttendanceDailyReport> queryWrapper = new LambdaQueryWrapper<>();

        // 根据班级ID过滤
        queryWrapper.in(AttendanceDailyReport::getClassId, classIds);

        // 根据查询类型设置日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate; // 默认为当天

        switch (granularity) {
            case 1: // 按日
                queryWrapper.eq(AttendanceDailyReport::getReportDate, startDate);
                break;
            case 2: // 按周：往前推6天，总共7天
                startDate = endDate.minusDays(6);
                queryWrapper.between(AttendanceDailyReport::getReportDate, startDate, endDate);
                break;
            case 3: // 按月：往前推29天，总共30天
                startDate = endDate.minusDays(29);
                queryWrapper.between(AttendanceDailyReport::getReportDate, startDate, endDate);
                break;
            case 4: //按学期
                queryWrapper.eq(AttendanceDailyReport::getSemesterName, semesterName);
                break;
        }

        // 按日期升序排列
        queryWrapper.orderByAsc(AttendanceDailyReport::getReportDate);

        return this.list(queryWrapper);
    }

}