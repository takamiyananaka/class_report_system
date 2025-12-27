package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程查询请求DTO
 */
@Data
@Schema(description = "课程查询请求参数")
public class CourseQueryDTO {

    @Schema(description = "教师工号", example = "T001")
    private String teacherNo;

    @Deprecated
    @Schema(description = "班级名称（已废弃，课程-班级关系现通过course_class表实现）", example = "25计算机类-1班")
    private String className;

    @Schema(description = "课程名称", example = "高等数学")
    private String courseName;

    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}