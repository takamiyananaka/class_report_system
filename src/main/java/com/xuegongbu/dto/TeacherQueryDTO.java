package com.xuegongbu.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 教师查询请求DTO
 */
@Data
@ApiModel(description = "教师查询请求参数")
public class TeacherQueryDTO {

    @ApiModelProperty(value = "教师工号", example = "T001")
    private String teacherNo;

    @ApiModelProperty(value = "部门/学院", example = "计算机学院")
    private String department;

    @ApiModelProperty(value = "真实姓名（支持模糊查询）", example = "张")
    private String realName;

    @ApiModelProperty(value = "电话号码", example = "13900139000")
    private String phone;

    @ApiModelProperty(value = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页大小", example = "10")
    private Integer pageSize = 10;
}
