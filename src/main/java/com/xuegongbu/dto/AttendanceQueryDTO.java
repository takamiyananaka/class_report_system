package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考勤记录查询请求DTO
 */
@Data
@Schema(description = "考勤记录查询请求参数")
public class AttendanceQueryDTO {

    @Schema(description = "课程ID", example = "123456789")
    private String courseId;

    @Schema(description = "开始时间", example = "2023-01-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2023-12-31T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}