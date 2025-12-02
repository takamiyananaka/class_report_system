package com.xuegongbu.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class CourseSchedule {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 教师ID（关联Teacher表）
     */
    private Long teacherId;
    
    /**
     * 班级名称
     */
    private String className;
    
    /**
     * 星期几（1-7）
     */
    private Integer weekday;
    
    /**
     * 开始时间
     */
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    private LocalTime endTime;
    
    /**
     * 教室
     */
    private String classroom;
    
    /**
     * 学期
     */
    private String semester;
    
    /**
     * 学年
     */
    private String schoolYear;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
