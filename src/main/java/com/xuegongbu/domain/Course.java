package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* 课程表
* @TableName course
*/
@TableName(value = "course")
@Data
@Schema(description = "课程表")
public class Course implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
    * 课程名称
    */
    @NotBlank(message="[课程名称]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @Schema(description = "课程名称")
    @Size(max= 100,message="编码长度不能超过100")
    private String courseName;
    /**
    * 课程编码
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "课程编码")
    @Size(max= 50,message="编码长度不能超过50")
    private String courseCode;
    /**
    * 教师工号
    */
    @NotNull(message="[教师工号]不能为空")
    @Schema(description = "教师工号")
    private String teacherNo;
    /**
    * 教室名
    */
    @NotBlank(message="[教室名]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "教室名")
    @Size(max= 50,message="编码长度不能超过50")
    private String classroom;
    /**
    * 上课时间（如：周一 1-2节）
    */
    @NotBlank(message="[上课时间（如：周一 1-2节）]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @Schema(description = "上课时间（如：周一 1-2节）")
    @Size(max= 100,message="编码长度不能超过100")
    private String courseTime;
    /**
    * 上课日期
    */
    @NotNull(message="[上课日期]不能为空")
    @Schema(description = "上课日期")
    private LocalDate courseDate;
    /**
    * 开始时间
    */
    @Schema(description = "开始时间")
    private LocalTime startTime;
    /**
    * 结束时间
    */
    @Schema(description = "结束时间")
    private LocalTime endTime;
    /**
    * 星期几：1-周一，7-周日
    */
    @Schema(description = "星期几：1-周一，7-周日")
    private Integer weekDay;
    /**
    * 预到人数
    */
    @Schema(description = "预到人数")
    private Integer expectedCount;
    /**
    * 学期（如：2024-2025-1）
    */
    @Size(max= 20,message="编码长度不能超过20")
    @Schema(description = "学期（如：2024-2025-1）")
    @Size(max= 20,message="编码长度不能超过20")
    private String semester;
    /**
    * 状态：0-已结束，1-进行中，2-未开始
    */
    @Schema(description = "状态：0-已结束，1-进行中，2-未开始")
    private Integer status;
    /**
    * 备注
    */
    @Size(max= 500,message="编码长度不能超过500")
    @Schema(description = "备注")
    @Size(max= 500,message="编码长度不能超过500")
    private String remark;
    /**
    * 创建时间
    */
    @Schema(description = "创建时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
    * 更新时间
    */
    @Schema(description = "更新时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
    * 是否删除：0-否，1-是
    */
    @Schema(description = "是否删除：0-否，1-是")
    @TableLogic
    private Integer isDeleted;

}