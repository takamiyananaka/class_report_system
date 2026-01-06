package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课表查询请求DTO
 */
@Data
@Schema(description = "课表查询请求参数")
public class CourseScheduleQueryDTO {

    @Schema(description = "教师工号", example = "T001")
    private String teacherNo;

    @Schema(description = "班级名称", example = "计算机2021级1班")
    private String className;

    @Schema(description = "课程名称", example = "Java程序设计")
    private String courseName;
    
    @Schema(description = "任课老师", example = "张老师")
    private String teacherName;
    
    @Schema(description = "课程类型（通识、专业课等）", example = "专业课")
    private String courseType;
    
    @Schema(description = "学期名（例如：2024-2025学年春季学期）", example = "2024-2025学年春季学期")
    private String semesterName;

    @Schema(description = "所属学院名称", example = "计算机与软件学院")
    private String collegeName;

    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}