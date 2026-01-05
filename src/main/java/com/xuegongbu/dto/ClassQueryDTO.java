package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 班级查询请求DTO
 */
@Data
@Schema(description = "班级查询请求参数")
public class ClassQueryDTO {

    @Schema(description = "班级名称（支持模糊查询）", example = "计算机")
    private String className;

    @Schema(description = "辅导员工号", example = "T001")
    private String teacherNo;

    @Schema(description = "年级列表（支持多选）", example = "[\"2023级\", \"2024级\"]")
    private java.util.List<String> grades;

    @Schema(description = "专业列表（支持多选）", example = "[\"计算机科学与技术\", \"软件工程\"]")
    private java.util.List<String> majors;

    @Schema(description = "学院名称", example = "计算机与软件学院")
    private String collegeName;

    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}