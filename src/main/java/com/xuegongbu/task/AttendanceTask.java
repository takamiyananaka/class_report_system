package com.xuegongbu.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuegongbu.domain.Alert;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.service.*;
import com.xuegongbu.util.ClassTimeUtil;
import com.xuegongbu.util.CountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class AttendanceTask {
    private final CourseScheduleService courseScheduleService;
    private final AttendanceService attendanceService;
    private final DeviceService deviceService;
    private final ClassService classService;
    private final CountUtil countUtil;
    private final AlertService alertService;
    private final ThreadPoolTaskScheduler taskScheduler;

    // 存储每天的定时任务
    private volatile ScheduledFuture<?> dailyAttendanceTask;
    private volatile ScheduledFuture<?> dailyScheduleTask;

    public AttendanceTask(CourseScheduleService courseScheduleService, 
                          AttendanceService attendanceService,
                          DeviceService deviceService,
                          ClassService classService,
                          CountUtil countUtil,
                          AlertService alertService) {
        this.courseScheduleService = courseScheduleService;
        this.attendanceService = attendanceService;
        this.deviceService = deviceService;
        this.classService = classService;
        this.countUtil = countUtil;
        this.alertService = alertService;
        
        // 创建任务调度器
        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.taskScheduler.setPoolSize(5);
        this.taskScheduler.setThreadNamePrefix("AttendanceTask-");
        this.taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        this.taskScheduler.setAwaitTerminationSeconds(30);
        this.taskScheduler.initialize();
    }

    /**
     * 每天凌晨1点执行，重置当天的考勤定时任务
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void resetDailyAttendanceTasks() {
        log.info("开始重置当天的考勤定时任务");

        // 取消之前的任务
        if (dailyAttendanceTask != null && !dailyAttendanceTask.isCancelled()) {
            dailyAttendanceTask.cancel(false);
        }

        if (dailyScheduleTask != null && !dailyScheduleTask.isCancelled()) {
            dailyScheduleTask.cancel(false);
        }

        // 为今天设置所有课程节次的考勤任务
        setupAttendanceTasksForToday();
    }

    /**
     * 设置今天的考勤任务
     */
    private void setupAttendanceTasksForToday() {
        log.info("设置今天的考勤任务");
        
        try {
            // 获取今天的日期
            LocalDate today = LocalDate.now();
            
            // 遍历所有课程节数，为每节课的第5分钟设置定时任务
            Integer[] allClassNumbers = ClassTimeUtil.getAllClassNumbers();
            for (Integer classNumber : allClassNumbers) {
                // 获取该节课的开始时间
                LocalTime classStartTime = ClassTimeUtil.getStartTimeAsLocalTime(classNumber);
                // 计算第5分钟的时间点
                LocalTime fifthMinuteTime = classStartTime.plusMinutes(5);
                
                // 创建今天的完整时间
                LocalDateTime taskTime = LocalDateTime.of(today, fifthMinuteTime);
                
                // 检查是否已经过了今天的时间，如果是，则设置为明天的相同时间
                if (taskTime.isBefore(LocalDateTime.now())) {
                    taskTime = taskTime.plusDays(1);
                }
                
                // 创建定时任务
                Date scheduledTime = Date.from(taskTime.atZone(ZoneId.systemDefault()).toInstant());
                
                // 检查任务时间是否在今天范围内（8:00-21:25）
                if (isWithinOperationalHours(fifthMinuteTime)) {
                    ScheduledFuture<?> task = taskScheduler.schedule(() -> {
                        try {
                            executeAutoAttendanceForClassPeriod(classNumber);
                        } catch (Exception e) {
                            log.error("执行第 {} 节课的考勤任务时发生错误", classNumber, e);
                        }
                    }, scheduledTime);
                    
                    log.info("已设置第 {} 节课的考勤任务，执行时间: {}", classNumber, taskTime);
                } else {
                    log.debug("第 {} 节课的考勤时间 {} 超出运营时间范围，跳过", classNumber, taskTime);
                }
            }
        } catch (Exception e) {
            log.error("设置考勤任务时发生错误", e);
        }
    }

    /**
     * 检查时间是否在运营时间内（8:00-21:25）
     */
    private boolean isWithinOperationalHours(LocalTime time) {
        return !time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(21, 25));
    }

    /**
     * 为特定课程节次执行自动考勤
     * @param classPeriod 课程节数
     */
    private void executeAutoAttendanceForClassPeriod(int classPeriod) {
        log.info("开始执行第 {} 节课的自动考勤任务", classPeriod);

        try {
            // 获取当前时间信息
            LocalDateTime now = LocalDateTime.now();
            DayOfWeek dayOfWeek = now.getDayOfWeek();
            String weekdayStr = ClassTimeUtil.convertDayOfWeekToChinese(dayOfWeek);

            log.info("当前时间 {} 是第 {} 节课的第5分钟，开始查询课程", now.toLocalTime(), classPeriod);

            // 查询当前正在进行的课程（根据星期几和当前课程节次的时间范围）
            QueryWrapper<CourseSchedule> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("weekday", weekdayStr)
                    .eq("start_time", ClassTimeUtil.getStartTimeAsLocalTime(classPeriod))
                    .eq("end_time", ClassTimeUtil.getEndTimeAsLocalTime(classPeriod));

            List<CourseSchedule> ongoingCourses = courseScheduleService.list(queryWrapper);

            log.info("找到 {} 个第 {} 节课的课程", ongoingCourses.size(), classPeriod);

            // 为每个正在进行的课程执行自动考勤
            for (CourseSchedule course : ongoingCourses) {
                try {
                    performAutoAttendance(course, now);
                } catch (Exception e) {
                    log.error("为课程 {} 执行自动考勤时发生错误: {}", course.getId(), e.getMessage(), e);
                }
            }

            log.info("第 {} 节课的自动考勤任务执行完成", classPeriod);
        } catch (Exception e) {
            log.error("执行第 {} 节课的考勤任务时发生错误", classPeriod, e);
        }
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

        Attendance existingAttendance = attendanceService.getOne(attendanceQuery);
        if (existingAttendance != null) {
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
        
        // 检查是否需要生成预警记录
        checkAndGenerateAlert(attendance, course);
    }
    
    /**
     * 检查并生成预警记录
     * @param attendance 考勤记录
     * @param course 课程安排
     */
    private void checkAndGenerateAlert(Attendance attendance, CourseSchedule course) {
        alertService.checkAndGenerateAlert(attendance, course);
    }
}