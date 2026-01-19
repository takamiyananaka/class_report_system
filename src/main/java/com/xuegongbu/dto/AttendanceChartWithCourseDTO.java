package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AttendanceChartWithCourseDTO {
    @Schema(description = "学院名称")
    private String collegeName;

    @Schema(description = "辅导员名字")
    private String teacherName;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "精细度：1-按日，2-按周，3-按月，4-按学期")
    private Integer granularity; // 1-daily, 2-weekly, 3-monthly, 4-termly

    @Schema(description = "学期名称")
    private String semesterName;
}
