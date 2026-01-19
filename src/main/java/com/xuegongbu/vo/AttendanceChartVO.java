package com.xuegongbu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "考勤图标vo对象")
public class AttendanceChartVO {
    @Schema(description = "日期")
    private String date;
    @Schema(description = "考勤率")
    private String attendRate;
}
