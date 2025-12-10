package com.xuegongbu.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 课程查询请求DTO
 */
@Data
@ApiModel(description = "课程查询请求参数")
public class CourseQueryDTO {

    @ApiModelProperty(value = "教师工号", example = "1")
    private Long teacherNo;

    @ApiModelProperty(value = "班级名称", example = "25计算机类-1班")
    private String className;

    @ApiModelProperty(value = "课程名称", example = "高等数学")
    private String courseName;

    @ApiModelProperty(value = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页大小", example = "10")
    private Integer pageSize = 10;
}
