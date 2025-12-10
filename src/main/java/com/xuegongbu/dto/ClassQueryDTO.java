package com.xuegongbu.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 班级查询请求DTO
 */
@Data
@ApiModel(description = "班级查询请求参数")
public class ClassQueryDTO {

    @ApiModelProperty(value = "班级名称（支持模糊查询）", example = "计算机")
    private String className;

    @ApiModelProperty(value = "辅导员工号", example = "T001")
    private String teacherNo;

    @ApiModelProperty(value = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页大小", example = "10")
    private Integer pageSize = 10;
}
