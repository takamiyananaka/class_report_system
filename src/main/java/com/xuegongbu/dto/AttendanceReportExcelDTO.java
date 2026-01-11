package com.xuegongbu.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考勤报表Excel导出DTO
 */
@Data
@ColumnWidth(25)
public class AttendanceReportExcelDTO {

    @ExcelProperty("课程ID")
    private String courseId;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("班级名称")
    private String className;

    @ExcelProperty("任课老师")
    private String teacherName;

    @ExcelProperty("考勤时间")
    private LocalDateTime checkTime;

    @ExcelProperty("应到人数")
    private Integer expectedCount;

    @ExcelProperty("实到人数")
    private Integer actualCount;

    @ExcelProperty("考勤率")
    private BigDecimal attendanceRate;

    @ExcelProperty("考勤类型")
    private Integer checkType;

    @ExcelProperty("状态")
    private Integer status;

    @ExcelProperty("备注")
    private String remark;
}