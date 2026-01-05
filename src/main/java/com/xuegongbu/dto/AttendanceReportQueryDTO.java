package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "考勤报表查询参数")
public class AttendanceReportQueryDTO {
    @Schema(description = "学院名称列表")
    List<String> collegeNames;

    @Schema(description = "教师工号列表")
    List<String> teacherNos;

    @Schema(description = "课序号列表")
    List<String> orderNos;

    @Schema(description = "课程类型列表")
    List<String> courseTypes;

    @Schema(description = "班级名称列表")
    List<String> classNames;

    @Schema(description = "开始时间")
    private LocalDate startDate;

    @Schema(description = "结束时间")
    private LocalDate endDate;

    @Schema(description = "学年")
    private String year;

    @Schema(description = "学期")
    private String semester;

    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}
