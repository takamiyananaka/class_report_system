package com.xuegongbu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CourseScheduleRequest {
    
    @NotBlank(message = "课程名称不能为空")
    private String courseName;
    
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;
    
    @NotBlank(message = "班级名称不能为空")
    private String className;
    
    @NotNull(message = "星期几不能为空")
    @Min(value = 1, message = "星期几必须在1-7之间")
    @Max(value = 7, message = "星期几必须在1-7之间")
    private Integer weekday;
    
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;
    
    @NotBlank(message = "教室不能为空")
    private String classroom;
    
    @NotBlank(message = "学期不能为空")
    private String semester;
    
    @NotBlank(message = "学年不能为空")
    private String schoolYear;
}
