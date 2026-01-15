package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 班级每日考勤报表
 */
@TableName(value = "attendance_daily_report")
@Data
@ApiModel(description = "班级每日考勤报表")
public class AttendanceDailyReport implements Serializable {

    /**
     * id
     */
    @NotNull(message = "[id]不能为空")
    @ApiModelProperty(value = "id")
    @TableId
    private String id;

    /**
     * 班级ID
     */
    @NotNull(message = "[班级ID]不能为空")
    @ApiModelProperty(value = "班级ID")
    private String classId;

    /**
     * 报表日期
     */
    @NotNull(message = "[报表日期]不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "报表日期")
    private LocalDate reportDate;

    /**
     * 考勤记录数
     */
    @ApiModelProperty(value = "考勤记录数")
    private Integer attendanceRecordCount;

    /**
     * 平均考勤率（%）
     */
    @ApiModelProperty(value = "平均考勤率（%）")
    private BigDecimal averageAttendanceRate;

    /**
     * 预警记录数
     */
    @ApiModelProperty(value = "预警记录数")
    private Integer alertRecordCount;

    /**
     * 预警率（%）
     */
    @ApiModelProperty(value = "预警率（%）")
    private BigDecimal alertRate;

    /**
     * 总预到人数
     */
    @ApiModelProperty(value = "总预到人数")
    private Integer totalExpectedCount;

    /**
     * 总实到人数
     */
    @ApiModelProperty(value = "总实到人数")
    private Integer totalActualCount;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}