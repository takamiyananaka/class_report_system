package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 预警记录查询请求DTO
 */
@Data
@Schema(description = "预警记录查询请求参数")
public class AlertQueryDTO {

    @Schema(description = "日期", example = "2023-01-01")
    private LocalDate date;

    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}