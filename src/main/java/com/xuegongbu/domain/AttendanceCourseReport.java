package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课程考勤报表表
 */
@Data
@TableName(value = "attendance_course_report")
@Schema(description = "课程考勤报表表")
public class AttendanceCourseReport implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 课序号
     */
    @Schema(description = "课序号")
    private String orderNo;

    /**
     * 报表日期
     */
    @Schema(description = "报表日期")
    private LocalDate reportDate;

    /**
     * 考勤记录数
     */
    @Schema(description = "考勤记录数")
    private Integer attendanceRecordCount;

    /**
     * 平均考勤率（%）
     */
    @Schema(description = "平均考勤率（%）")
    private BigDecimal averageAttendanceRate;

    /**
     * 预警记录数
     */
    @Schema(description = "预警记录数")
    private Integer alertRecordCount;

    /**
     * 预警率（%）
     */
    @Schema(description = "预警率（%）")
    private BigDecimal alertRate;

    /**
     * 总预到人数
     */
    @Schema(description = "总预到人数")
    private Integer totalExpectedCount;

    /**
     * 总实到人数
     */
    @Schema(description = "总实到人数")
    private Integer totalActualCount;

    /**
     * 学期名称
     */
    @Schema(description = "学期名称")
    private String semesterName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}