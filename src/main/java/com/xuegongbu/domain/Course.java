package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 课程表
* @TableName tb_course
*/
@TableName(value = "tb_course")
@Data
public class Course implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
    * 课程名称
    */
    @NotBlank(message="[课程名称]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("课程名称")
    @Length(max= 100,message="编码长度不能超过100")
    private String courseName;
    /**
    * 课程编码
    */
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("课程编码")
    @Length(max= 50,message="编码长度不能超过50")
    private String courseCode;
    /**
    * 教师ID
    */
    @NotNull(message="[教师ID]不能为空")
    @ApiModelProperty("教师ID")
    private Long teacherId;
    /**
    * 教室号
    */
    @NotBlank(message="[教室号]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("教室号")
    @Length(max= 50,message="编码长度不能超过50")
    private String classroom;
    /**
    * 上课时间（如：周一 1-2节）
    */
    @NotBlank(message="[上课时间（如：周一 1-2节）]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("上课时间（如：周一 1-2节）")
    @Length(max= 100,message="编码长度不能超过100")
    private String courseTime;
    /**
    * 上课日期
    */
    @NotNull(message="[上课日期]不能为空")
    @ApiModelProperty("上课日期")
    private LocalDate courseDate;
    /**
    * 开始时间
    */
    @ApiModelProperty("开始时间")
    private LocalTime startTime;
    /**
    * 结束时间
    */
    @ApiModelProperty("结束时间")
    private LocalTime endTime;
    /**
    * 星期几：1-周一，7-周日
    */
    @ApiModelProperty("星期几：1-周一，7-周日")
    private Integer weekDay;
    /**
    * 预到人数
    */
    @ApiModelProperty("预到人数")
    private Integer expectedCount;
    /**
    * 学期（如：2024-2025-1）
    */
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("学期（如：2024-2025-1）")
    @Length(max= 20,message="编码长度不能超过20")
    private String semester;
    /**
    * 状态：0-已结束，1-进行中，2-未开始
    */
    @ApiModelProperty("状态：0-已结束，1-进行中，2-未开始")
    private Integer status;
    /**
    * 备注
    */
    @Size(max= 500,message="编码长度不能超过500")
    @ApiModelProperty("备注")
    @Length(max= 500,message="编码长度不能超过500")
    private String remark;
    /**
    * 创建时间
    */
    @ApiModelProperty("创建时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
    * 更新时间
    */
    @ApiModelProperty("更新时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
    * 是否删除：0-否，1-是
    */
    @ApiModelProperty("是否删除：0-否，1-是")
    @TableLogic
    private Integer isDeleted;
    /**
     * 上课的班级名字
     */
    @ApiModelProperty("上课的班级名字")
    private String className;


}
