package com.xuegongbu.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 课表Excel导入DTO
 */
@Data
public class CourseScheduleExcelDTO implements Serializable {

    /**
     * 课程名称
     */
    @ExcelProperty(index = 0, value = "课程名称")
    private String courseName;

    /**
     * 教师ID
     */
    @ExcelProperty(index = 1, value = "教师ID")
    private Long teacherId;

    /**
     * 班级名称
     */
    @ExcelProperty(index = 2, value = "班级名称")
    private String className;

    /**
     * 星期几（1-7）
     */
    @ExcelProperty(index = 3, value = "星期几")
    private Integer weekday;

    /**
     * 开始时间 (格式: HH:mm:ss 或 HH:mm)
     */
    @ExcelProperty(index = 4, value = "开始时间")
    private String startTime;

    /**
     * 结束时间 (格式: HH:mm:ss 或 HH:mm)
     */
    @ExcelProperty(index = 5, value = "结束时间")
    private String endTime;

    /**
     * 教室
     */
    @ExcelProperty(index = 6, value = "教室")
    private String classroom;

    /**
     * 学期
     */
    @ExcelProperty(index = 7, value = "学期")
    private String semester;

    /**
     * 学年
     */
    @ExcelProperty(index = 8, value = "学年")
    private String schoolYear;
}
