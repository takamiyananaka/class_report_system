package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课表查询请求DTO
 */
@Data
@Schema(description = "课表查询请求参数")
public class CourseScheduleQueryDTO {

    @Schema(description = "教师工号", example = "1")
    private Long teacherNo;

    @Schema(description = "班级名称", example = "计算机2021级1班")
    private String className;

    @Schema(description = "课程名称", example = "Java程序设计")
    private String courseName;

    @Schema(description = "星期几（1-7）", example = "1")
    private Integer weekday;

    @Schema(description = "学期", example = "第一学期")
    private String semester;

    @Schema(description = "学年", example = "2024-2025")
    private String schoolYear;

    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}