package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.domain.Alert;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.mapper.AlertMapper;
import com.xuegongbu.mapper.CourseScheduleMapper;
import com.xuegongbu.service.AlertService;
import com.xuegongbu.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {
    
    @Autowired
    private CourseScheduleMapper courseScheduleMapper;
    
    @Autowired
    private MailService mailService;

    @Override
    public List<Alert> listByTeacherId(String teacherNo) {
        // 根据教师ID查询其关联的课程安排
        QueryWrapper<CourseSchedule> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("teacher_no", teacherNo);
        List<CourseSchedule> courses = courseScheduleMapper.selectList(courseQueryWrapper);
        
        if (courses.isEmpty()) {
            return List.of(); // 返回空列表
        }
        
        // 提取课程ID列表
        List<String> courseIds = courses.stream()
                .map(CourseSchedule::getId)
                .toList();
        
        // 根据课程ID列表查询预警记录
        QueryWrapper<Alert> alertQueryWrapper = new QueryWrapper<>();
        alertQueryWrapper.in("course_id", courseIds)
                .orderByDesc("create_time");
        
        return list(alertQueryWrapper);
    }
    
    /**
     * 检查考勤记录并生成相应的预警
     * @param attendance 考勤记录
     * @param course 课程安排
     */
    @Override
    public void checkAndGenerateAlert(Attendance attendance, CourseSchedule course) {
        try {
            BigDecimal attendanceRate = attendance.getAttendanceRate();
            
            // 根据出勤率生成不同级别的预警
            if (attendanceRate.compareTo(BigDecimal.valueOf(0.95)) < 0) {
                Alert alert = new Alert();
                alert.setCourseId(course.getId());
                alert.setAttendanceId(attendance.getId());
                alert.setExpectedCount(attendance.getExpectedCount());
                alert.setActualCount(attendance.getActualCount());
                
                // 设置预警类型和级别
                alert.setAlertType(1); // 人数不足
                
                if (attendanceRate.compareTo(BigDecimal.valueOf(0.80)) >= 0) {
                    // 80%到95%为低级别
                    alert.setAlertLevel(1); // 低级别
                } else if (attendanceRate.compareTo(BigDecimal.valueOf(0.60)) >= 0) {
                    // 60%到80%为中级别
                    alert.setAlertLevel(2); // 中级别
                } else {
                    // 60%以下为高级别
                    alert.setAlertLevel(3); // 高级别
                }
                
                // 根据不同级别设置不同的预警消息
                String messageTemplate;
                if (alert.getAlertLevel() == 1) {
                    messageTemplate = "课程 %s 出勤率偏低，实到 %d 人，应到 %d 人，出勤率 %.2f%%";
                } else if (alert.getAlertLevel() == 2) {
                    messageTemplate = "课程 %s 出勤率严重偏低，实到 %d 人，应到 %d 人，出勤率 %.2f%%";
                } else {
                    messageTemplate = "课程 %s 出勤率极度偏低，实到 %d 人，应到 %d 人，出勤率 %.2f%%";
                }
                
                alert.setAlertMessage(String.format(messageTemplate, 
                        course.getCourseName(), 
                        attendance.getActualCount(), 
                        attendance.getExpectedCount(),
                        attendanceRate.multiply(BigDecimal.valueOf(100)).doubleValue()));
                
                save(alert);
                
                // 直接发送邮件通知
                mailService.sendAlertNotification(alert);
                
                log.info("为课程 {} 生成了{}预警记录，出勤率: {}%", course.getCourseName(), 
                        alert.getAlertLevel() == 1 ? "低级别" : (alert.getAlertLevel() == 2 ? "中级别" : "高级别"),
                        attendanceRate.multiply(BigDecimal.valueOf(100)).intValue());
            }

        } catch (Exception e) {
            log.error("检查并生成预警记录时发生错误: {}", e.getMessage(), e);
        }
    }
}