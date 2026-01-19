package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "考勤图表查询参数（课程）")
public class AttendanceChartWithCourseDTO {
    @Schema(description = "任课教师名字",example = "李副教授")
    private String teacherName;

    @Schema(description = "课序号",example = "01")
    private String orderNo;

    @Schema(description = "学期名称",example = "2025-2026学年秋季学期")
    private String semesterName;

    @Schema(description = "精细度：1-按日，2-按周，3-按月，4-按学期",example = "1")
    @NotNull(message = "请选择精细度")
    private Integer granularity; // 1-daily, 2-weekly, 3-monthly, 4-termly
}
