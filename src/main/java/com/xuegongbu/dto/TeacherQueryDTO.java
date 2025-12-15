package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

/**
 * 教师查询请求DTO
 */
@Data
@Schema(description = "教师查询请求参数")
public class TeacherQueryDTO {

    @Schema(description = "教师工号", example = "T001")
    private String teacherNo;

    @Schema(description = "部门/学院", example = "计算机学院")
    private String department;

    @Schema(description = "真实姓名（支持模糊查询）", example = "张")
    private String realName;

    @Schema(description = "电话号码", example = "13900139000")
    private String phone;

    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}