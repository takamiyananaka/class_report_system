package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学期请求DTO
 */
@Data
@Schema(description = "学期请求DTO")
public class SemesterRequest {

    @Schema(description = "学期名，例如：2024-2025学年春季学期")
    private String semesterName;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "周数")
    private Integer weeks;
}