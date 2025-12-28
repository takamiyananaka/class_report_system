package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuegongbu.domain.Alert;
import com.xuegongbu.domain.Class;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.mapper.AlertMapper;
import com.xuegongbu.mapper.ClassMapper;
import com.xuegongbu.mapper.CourseScheduleMapper;
import com.xuegongbu.mapper.TeacherMapper;
import com.xuegongbu.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private CourseScheduleMapper courseScheduleMapper;
    
    @Autowired
    private ClassMapper classMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Autowired
    private AlertMapper alertMapper;

    @Override
    @Async
    @Retryable(value = {MessagingException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendAlertNotification(Alert alert,Teacher  teacher) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            // 获取课程信息
            CourseSchedule course = courseScheduleMapper.selectById(alert.getCourseId());
            
            // 获取班级信息
            Class alertClass = classMapper.selectById(alert.getClassId());

            // 设置邮件内容
            helper.setFrom(fromEmail);
            // 暂时使用固定邮箱
            helper.setTo("1554318155@qq.com");
            helper.setSubject("课程考勤预警通知 - " + (course != null ? course.getCourseName() : "未知课程"));
            
            // 使用新模板生成邮件内容
            String content = generateEmailContent(alert, course, alertClass, teacher);
            helper.setText(content, true);
            
            mailSender.send(mimeMessage);
            log.info("成功发送预警邮件通知，课程ID: {}, 班级ID: {}, 预警级别: {}", 
                    alert.getCourseId(), alert.getClassId(), alert.getAlertLevel());
            //更新预警状态为已发送
            alert.setNotifyStatus(1);
            alert.setNotifyTime(LocalDateTime.now());
            alertMapper.updateById(alert);
        } catch (Exception e) {
            log.error("发送预警邮件通知失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 使用精美模板生成邮件内容
     */
    private String generateEmailContent(Alert alert, CourseSchedule course, Class alertClass, Teacher teacher) {
        try {
            // 读取模板文件
            ClassPathResource resource = new ClassPathResource("templates/alert-notification.html");
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // 替换模板中的占位符
            String content = template
                .replace("{{courseName}}", course != null ? course.getCourseName() : "未知课程")
                .replace("{{className}}", alertClass != null ? alertClass.getClassName() : "未知班级")
                .replace("{{teacherName}}", teacher != null ? teacher.getRealName() : "未知教师")
                .replace("{{expectedCount}}", alert.getExpectedCount() != null ? alert.getExpectedCount().toString() : "N/A")
                .replace("{{actualCount}}", alert.getActualCount() != null ? alert.getActualCount().toString() : "N/A");
            
            // 处理上课时间
            if (course != null) {
                String courseTime = "星期" + course.getWeekday() + " " + course.getWeekRange() + " 第" + 
                                  course.getStartPeriod() + "-" + course.getEndPeriod() + "节";
                content = content.replace("{{courseTime}}", courseTime);
                content = content.replace("{{classroom}}", course.getClassroom());
            } else {
                content = content.replace("{{courseTime}}", "未知时间");
                content = content.replace("{{classroom}}", "未知教室");
            }
            
            // 计算出勤率
            String attendanceRate = "N/A";
            String rateClass = "";
            if (alert.getExpectedCount() != null && alert.getActualCount() != null && alert.getExpectedCount() > 0) {
                double rate = (double) alert.getActualCount() * 100 / alert.getExpectedCount();
                attendanceRate = String.format("%.2f%%", rate);
                
                // 根据出勤率设置样式
                if (rate >= 90) {
                    rateClass = "rate-high"; // 绿色，表示出勤率高
                } else if (rate >= 80) {
                    rateClass = "rate-medium"; // 黄色，表示出勤率中等
                } else {
                    rateClass = "rate-low"; // 红色，表示出勤率低
                }
            }
            content = content.replace("{{attendanceRate}}", attendanceRate);
            content = content.replace("{{rateClass}}", rateClass);
            
            // 处理预警级别
            String alertLevel = "未知";
            String alertLevelClass = "";
            if (alert.getAlertLevel() != null) {
                switch (alert.getAlertLevel()) {
                    case 1: 
                        alertLevel = "低";
                        alertLevelClass = "alert-low";
                        break;
                    case 2: 
                        alertLevel = "中";
                        alertLevelClass = "alert-medium";
                        break;
                    case 3: 
                        alertLevel = "高";
                        alertLevelClass = "alert-high";
                        break;
                    default: 
                        alertLevel = "未知";
                        alertLevelClass = "";
                }
            }
            content = content.replace("{{alertLevel}}", alertLevel);
            content = content.replace("{{alertLevelClass}}", alertLevelClass);
            
            // 处理预警信息
            content = content.replace("{{alertMessage}}", alert.getAlertMessage() != null ? alert.getAlertMessage() : "");
            
            // 处理创建时间
            String createTime = "";
            if (alert.getCreateTime() != null) {
                createTime = alert.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            content = content.replace("{{createTime}}", createTime);
            
            return content;
        } catch (Exception e) {
            log.error("生成邮件内容失败，使用默认模板: {}", e.getMessage(), e);
            // 出现异常时回退到原来的简单模板
            return generateFallbackContent(alert, course, alertClass, teacher);
        }
    }
    
    /**
     * 回退到原来的简单模板
     */
    private String generateFallbackContent(Alert alert, CourseSchedule course, Class alertClass, Teacher teacher) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>课程考勤预警通知</h2>");
        content.append("<p>课程名称: ").append(course != null ? course.getCourseName() : "未知课程").append("</p>");
        content.append("<p>班级名称: ").append(alertClass != null ? alertClass.getClassName() : "未知班级").append("</p>");
        content.append("<p>授课教师: ").append(teacher != null ? teacher.getRealName() : "未知教师").append("</p>");
        
        if (course != null) {
            content.append("<p>上课时间: 星期").append(course.getWeekday()).append(" ")
                   .append(course.getWeekRange()).append(" 第")
                   .append(course.getStartPeriod()).append("-").append(course.getEndPeriod()).append("节</p>");
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
        
        return content.toString();
    }
}