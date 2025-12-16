package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuegongbu.domain.Alert;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.mapper.AlertMapper;
import com.xuegongbu.mapper.CourseScheduleMapper;
import com.xuegongbu.mapper.TeacherMapper;
import com.xuegongbu.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private CourseScheduleMapper courseScheduleMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Autowired
    private AlertMapper alertMapper;

    @Override
    @Async
    @Retryable(value = {MessagingException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendAlertNotification(Alert alert) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            // 获取课程信息
            CourseSchedule course = courseScheduleMapper.selectById(alert.getCourseId());
            
            // 获取教师信息
            Teacher teacher = null;
            if (course != null) {
                LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teacher::getTeacherNo, course.getTeacherNo());
                teacher = teacherMapper.selectOne(queryWrapper);
            }
            
            // 设置邮件内容
            helper.setFrom(fromEmail);
            // 暂时使用固定邮箱
            helper.setTo("1554318155@qq.com");
            helper.setSubject("课程考勤预警通知 - " + (course != null ? course.getCourseName() : "未知课程"));
            
            StringBuilder content = new StringBuilder();
            content.append("<html><body>");
            content.append("<h2>课程考勤预警通知</h2>");
            content.append("<p>课程名称: ").append(course != null ? course.getCourseName() : "未知课程").append("</p>");
            content.append("<p>授课教师: ").append(teacher != null ? teacher.getRealName() : "未知教师").append("</p>");
            
            if (course != null) {
                content.append("<p>上课时间: 星期").append(course.getWeekday()).append(" ")
                       .append(course.getStartTime()).append("-").append(course.getEndTime()).append("</p>");
                content.append("<p>教室: ").append(course.getClassroom()).append("</p>");
            }
            
            content.append("<p>应到人数: ").append(alert.getExpectedCount()).append("</p>");
            content.append("<p>实到人数: ").append(alert.getActualCount()).append("</p>");
            content.append("<p>出勤率: ").append(String.format("%.2f%%", 
                   alert.getActualCount() * 100.0 / alert.getExpectedCount())).append("</p>");
            content.append("<p>预警级别: ");
            
            switch (alert.getAlertLevel()) {
                case 1 -> content.append("低");
                case 2 -> content.append("中");
                case 3 -> content.append("高");
                default -> content.append("未知");
            }
            
            content.append("</p>");
            content.append("<p>预警信息: ").append(alert.getAlertMessage()).append("</p>");
            content.append("<p>生成时间: ").append(alert.getCreateTime()).append("</p>");
            content.append("</body></html>");
            
            helper.setText(content.toString(), true);
            
            mailSender.send(mimeMessage);
            log.info("成功发送预警邮件通知，课程ID: {}, 预警级别: {}", alert.getCourseId(), alert.getAlertLevel());
            //更新预警状态为已发送
            alert.setNotifyStatus(1);
            alert.setNotifyTime(LocalDateTime.now());
            alertMapper.updateById(alert);
        } catch (Exception e) {
            log.error("发送预警邮件通知失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
}