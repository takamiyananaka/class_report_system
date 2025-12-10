package com.xuegongbu.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 课表查询请求DTO
 */
@Data
@ApiModel(description = "课表查询请求参数")
public class CourseScheduleQueryDTO {

    @ApiModelProperty(value = "教师ID", example = "1")
    private Long teacherId;

    @ApiModelProperty(value = "班级名称", example = "计算机2021级1班")
    private String className;

    @ApiModelProperty(value = "课程名称", example = "Java程序设计")
    private String courseName;

    @ApiModelProperty(value = "星期几（1-7）", example = "1")
    private Integer weekday;

    @ApiModelProperty(value = "学期", example = "第一学期")
    private String semester;

    @ApiModelProperty(value = "学年", example = "2024-2025")
    private String schoolYear;

    @ApiModelProperty(value = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页大小", example = "10")
    private Integer pageSize = 10;
}
