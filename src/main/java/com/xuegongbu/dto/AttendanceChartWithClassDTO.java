package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AttendanceChartWithClassDTO {
    @Schema(description = "任课教师名字")
    private String teacherName;

    @Schema(description = "课序号")
    private String orderNo;

    @Schema(description = "学期名称")
    private String semesterName;

    @Schema(description = "精细度：1-按日，2-按周，3-按月，4-按学期")
    private Integer granularity; // 1-daily, 2-weekly, 3-monthly, 4-termly
}
