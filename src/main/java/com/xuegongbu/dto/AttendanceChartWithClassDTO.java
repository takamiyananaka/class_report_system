package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "考勤图表数据(班级)")
public class AttendanceChartWithClassDTO {

    @Schema(description = "学院名称",example = "计算机与软件学院")
    private String collegeName;

    @Schema(description = "辅导员名字",example = "张老师")
    private String teacherName;

    @Schema(description = "班级名称",example = "24计科4班")
    private String className;

    @Schema(description = "精细度：1-按日，2-按周，3-按月，4-按学期",example = "1")
    @NotNull(message = "请选择精细度")
    private Integer granularity; // 1-daily, 2-weekly, 3-monthly, 4-termly

    @Schema(description = "学期名称",example = "2025-2026学年秋季学期")
    private String semesterName;
}
