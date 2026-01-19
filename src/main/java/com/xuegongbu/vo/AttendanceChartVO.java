package com.xuegongbu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "考勤图标vo对象")
public class AttendanceChartVO {
    @Schema(description = "日期")
    private LocalDate date;
    @Schema(description = "考勤率")
    private BigDecimal attendRate;
}
