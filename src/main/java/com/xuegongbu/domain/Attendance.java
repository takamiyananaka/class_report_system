package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 考勤记录表
* @TableName attendance
*/
@TableName(value = "attendance")
@Data
public class Attendance implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
    * 课程ID
    */
    @NotNull(message="[课程ID]不能为空")
    @ApiModelProperty("课程ID")
    private Long courseId;
    /**
    * 考勤时间
    */
    @NotNull(message="[考勤时间]不能为空")
    @ApiModelProperty("考勤时间")
    private LocalDateTime checkTime;
    /**
    * 实到人数
    */
    @ApiModelProperty("实到人数")
    private Integer actualCount;
    /**
    * 预到人数
    */
    @ApiModelProperty("预到人数")
    private Integer expectedCount;
    /**
    * 出勤率（%）
    */
    @ApiModelProperty("出勤率（%）")
    private BigDecimal attendanceRate;
    /**
    * 抓取的图片URL
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("抓取的图片URL")
    @Size(max= 255,message="编码长度不能超过255")
    private String imageUrl;
    /**
    * 考勤类型：1-自动，2-手动
    */
    @ApiModelProperty("考勤类型：1-自动，2-手动")
    private Integer checkType;
    /**
    * 状态：1-正常，2-异常
    */
    @ApiModelProperty("状态：1-正常，2-异常")
    private Integer status;
    /**
    * 备注
    */
    @Size(max= 500,message="编码长度不能超过500")
    @ApiModelProperty("备注")
    @Size(max= 500,message="编码长度不能超过500")
    private String remark;
    /**
    * 创建时间
    */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    /**
    * 更新时间
    */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

}
