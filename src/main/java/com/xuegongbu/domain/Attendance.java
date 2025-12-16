package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* 考勤记录表
* @TableName attendance
*/
@TableName(value = "attendance")
@Data
@Schema(description = "考勤记录表")
public class Attendance implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
    * 课程ID
    */
    @NotNull(message="[课程ID]不能为空")
    @Schema(description = "课程ID")
    private String courseId;
    /**
    * 考勤时间
    */
    @NotNull(message="[考勤时间]不能为空")
    @Schema(description = "考勤时间")
    private LocalDateTime checkTime;
    /**
    * 实到人数
    */
    @Schema(description = "实到人数")
    private Integer actualCount;
    /**
    * 预到人数
    */
    @Schema(description = "预到人数")
    private Integer expectedCount;
    /**
    * 出勤率（%）
    */
    @Schema(description = "出勤率（%）")
    private BigDecimal attendanceRate;
    /**
    * 抓取的图片URL
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "抓取的图片URL")
    @Size(max= 255,message="编码长度不能超过255")
    private String imageUrl;
    /**
    * 考勤类型：1-自动，2-手动
    */
    @Schema(description = "考勤类型：1-自动，2-手动")
    private Integer checkType;
    /**
    * 状态：1-正常，2-异常
    */
    @Schema(description = "状态：1-正常，2-异常")
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
    * 更新时间
    */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
    * 逻辑删除
    */
    @Schema(description = "逻辑删除")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}