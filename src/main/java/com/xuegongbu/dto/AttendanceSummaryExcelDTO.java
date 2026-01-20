package com.xuegongbu.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 考勤汇总报表Excel导出DTO
 */
@Data
@ColumnWidth(25)
public class AttendanceSummaryExcelDTO {

    @ExcelProperty("id")
    private String identifier;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("时间范围内的平均考勤率")
    private BigDecimal overallAttendanceRate;

    @ExcelProperty("月度平均考勤率")
    private BigDecimal monthlyAttendanceRate;

    @ExcelProperty("学期平均考勤率")
    private BigDecimal semesterAttendanceRate;
    
    @ExcelProperty("统计时间范围")
    private String statisticsTime;
}