package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.domain.*;
import com.xuegongbu.domain.Class;
import com.xuegongbu.dto.AlertQueryDTO;
import com.xuegongbu.mapper.*;
import com.xuegongbu.service.AlertService;
import com.xuegongbu.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {
    
    @Autowired
    private CourseScheduleMapper courseScheduleMapper;
    
    @Autowired
    private MailService mailService;
    @Autowired
    private AttendanceMapper attendanceMapper;
    @Autowired
    private ClassMapper classMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;

    /**
     * 分页查询教师关联的预警记录（支持日期查询）
     * @param teacherNo 教师工号
     * @param queryDTO 查询参数
     * @return
     */
    @Override
    public Page<Alert> getAlertList(AlertQueryDTO queryDTO, String teacherNo) {
        // 设置分页参数
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
        Page<Alert> page = new Page<>(pageNum, pageSize);
        
        List<String> classIds = new java.util.ArrayList<>();
        

        if (queryDTO.getClassIds() != null && !queryDTO.getClassIds().isEmpty()) {
            // 如果指定了班级ID数组，根据班级ID获取课程ID
            classIds =queryDTO.getClassIds();
        } else {
            // 如果没有指定班级ID，则根据教师工号获取班级ID
            QueryWrapper<Class> classQueryWrapper = new QueryWrapper<>();
            classQueryWrapper.eq("teacher_no", teacherNo);
            List<Class> classes = classMapper.selectList(classQueryWrapper);
        }
        
        if (classIds.isEmpty()) {
            // 如果没有课程，返回空分页结果
            return page;
        }
        
        // 根据课程ID列表查询预警记录
        QueryWrapper<Alert> alertQueryWrapper = new QueryWrapper<>();
        alertQueryWrapper.in("class_id", classIds);
        
        // 添加日期查询条件
        if (queryDTO.getDate() != null) {
            // 构造一天的开始和结束时间
            LocalDateTime startOfDay = queryDTO.getDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            alertQueryWrapper.ge("create_time", startOfDay)
                           .lt("create_time", endOfDay);
        }
        
        alertQueryWrapper.orderByDesc("create_time");
        
        return page(page, alertQueryWrapper);
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
                attendance.setStatus(0);
                attendanceMapper.updateById( attendance);
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
                    messageTemplate = "课程 %s (班级: %s) 出勤率偏低，实到 %d 人，应到 %d 人，出勤率 %.2f%%";
                } else if (alert.getAlertLevel() == 2) {
                    messageTemplate = "课程 %s (班级: %s) 出勤率严重偏低，实到 %d 人，应到 %d 人，出勤率 %.2f%%";
                } else {
                    messageTemplate = "课程 %s (班级: %s) 出勤率极度偏低，实到 %d 人，应到 %d 人，出勤率 %.2f%%";
                }
                

                // 查询课程关联的所有班级
                QueryWrapper<Class> classQueryWrapper = new QueryWrapper<>();
                classQueryWrapper.eq("course_id", course.getId());
                List<Class> classList = classMapper.selectList(classQueryWrapper);
                
                // 为每个班级创建一个预警记录
                for (Class cls : classList) {
                    //获取班级老师
                    QueryWrapper<Teacher> teacherQueryWrapper = new QueryWrapper<>();
                    teacherQueryWrapper.eq("teacher_no", cls.getTeacherNo());
                    Teacher teacher = teacherMapper.selectOne(teacherQueryWrapper);
                    //创建预警记录
                    Alert classAlert = new Alert();
                    classAlert.setCourseId(course.getId());
                    classAlert.setClassId(cls.getId()); // 设置班级ID
                    classAlert.setAttendanceId(attendance.getId());
                    classAlert.setExpectedCount(attendance.getExpectedCount());
                    classAlert.setActualCount(attendance.getActualCount());
                    classAlert.setAlertType(alert.getAlertType());
                    classAlert.setAlertLevel(alert.getAlertLevel());
                    
                    classAlert.setAlertMessage(String.format(messageTemplate, 
                            course.getCourseName(), 
                            cls.getClassName(),
                            attendance.getActualCount(), 
                            attendance.getExpectedCount(),
                            attendanceRate.multiply(BigDecimal.valueOf(100)).doubleValue()));
                    classAlert.setReadStatus(0);
                    classAlert.setNotifyStatus(0);
                    save(classAlert);
                    
                    // 检查教师是否开启邮件通知
                    Integer enableEmailNotification = teacher.getEnableEmailNotification();
                    if (enableEmailNotification != null && enableEmailNotification == 1) {
                        // 发送邮件通知
                        mailService.sendAlertNotification(classAlert, teacher);
                    } else {
                        log.info("教师 {} 已关闭邮件通知，不发送邮件", teacher.getTeacherNo());
                        // 更新预警状态为已发送（实际上没有发送邮件）
                        classAlert.setNotifyStatus(1);
                        classAlert.setNotifyTime(LocalDateTime.now());
                        updateById(classAlert);
                    }
                    
                    log.info("为课程 {} (班级: {}) 生成了{}预警记录，出勤率: {}%", course.getCourseName(),
                            cls.getClassName(),
                            alert.getAlertLevel() == 1 ? "低级别" : (alert.getAlertLevel() == 2 ? "中级别" : "高级别"),
                            attendanceRate.multiply(BigDecimal.valueOf(100)).intValue());
                }


            }

        } catch (Exception e) {
            log.error("检查并生成预警记录时发生错误: {}", e.getMessage(), e);
        }
    }
}