package com.xuegongbu.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.dto.CountResponse;
import com.xuegongbu.service.*;
import com.xuegongbu.util.CountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AttendanceTask {
    private final CourseScheduleService courseScheduleService;
    private final AttendanceService attendanceService;
    private final DeviceService deviceService;
    private final ClassService classService;
    private final CountUtil countUtil;

    public AttendanceTask(CourseScheduleService courseScheduleService, 
                          AttendanceService attendanceService,
                          DeviceService deviceService,
                          ClassService classService,
                          CountUtil countUtil) {
        this.courseScheduleService = courseScheduleService;
        this.attendanceService = attendanceService;
        this.deviceService = deviceService;
        this.classService = classService;
        this.countUtil = countUtil;
    }

    /**
     * 自动考勤任务
     * 从每天8:05开始，每30分钟执行一次，检查当前是否有正在进行的课程，如果有则执行自动考勤
     */
    @Scheduled(cron = "0 5/30 8-23 * * ?")
    public void autoAttendance() {
        log.info("开始执行自动考勤任务");

        // 获取当前时间信息
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        // 查询当前正在进行的课程（根据星期几和时间范围）
        QueryWrapper<CourseSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("weekday", dayOfWeek.getValue())
                .le("start_time", currentTime)
                .ge("end_time", currentTime);

        List<CourseSchedule> ongoingCourses = courseScheduleService.list(queryWrapper);

        log.info("找到 {} 个正在进行的课程", ongoingCourses.size());

        // 为每个正在进行的课程执行自动考勤
        for (CourseSchedule course : ongoingCourses) {
            try {
                performAutoAttendance(course, now);
            } catch (Exception e) {
                log.error("为课程 {} 执行自动考勤时发生错误: {}", course.getId(), e.getMessage(), e);
            }
        }

        log.info("自动考勤任务执行完成");
    }

    /**
     * 为特定课程执行自动考勤
     *
     * @param course 课程安排
     * @param checkTime 考勤时间
     */
    private void performAutoAttendance(CourseSchedule course, LocalDateTime checkTime) {
        log.info("开始为课程 {} 执行自动考勤", course.getId());

        // 检查是否已经存在该课程在当前时间段的考勤记录
        QueryWrapper<Attendance> attendanceQuery = new QueryWrapper<>();
        attendanceQuery.eq("course_id", course.getId())
                .eq("check_time", checkTime.truncatedTo(ChronoUnit.MINUTES));

        if (attendanceService.getOne(attendanceQuery) != null) {
            log.info("课程 {} 在 {} 已有考勤记录，跳过", course.getId(), checkTime);
            return;
        }

        // 调用考勤服务类的手动考勤方法
        Attendance attendance = attendanceService.manualAttendance(course.getId());
        
        // 修改备注和状态为自动考勤
        attendance.setCheckType(1); // 自动考勤
        attendance.setStatus(1); // 正常状态
        attendance.setRemark("自动考勤");
        attendanceService.updateById(attendance);
        
        log.info("课程 {} 自动考勤完成，实到 {} 人，应到 {} 人，出勤率 {}%", 
                course.getCourseName(), attendance.getActualCount(), attendance.getExpectedCount(),
                attendance.getAttendanceRate().multiply(BigDecimal.valueOf(100)).intValue());
    }
}