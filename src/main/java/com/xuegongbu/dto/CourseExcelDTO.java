package com.xuegongbu.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 课程Excel导入DTO
 */
@Data
public class CourseExcelDTO implements Serializable {

    /**
     * 课程名称
     */
    @ExcelProperty(index = 0, value = "课程名称")
    private String courseName;

    /**
     * 课程编码
     */
    @ExcelProperty(index = 1, value = "课程编码")
    private String courseCode;

    /**
     * 班级ID（多个班级用逗号分隔）
     */
    @ExcelProperty(index = 2, value = "班级ID")
    private String classIds;

    /**
     * 教室
     */
    @ExcelProperty(index = 3, value = "教室")
    private String classroom;

    /**
     * 上课时间（如：周一 1-2节）
     */
    @ExcelProperty(index = 4, value = "上课时间")
    private String courseTime;

    /**
     * 星期几（1-7）
     */
    @ExcelProperty(index = 5, value = "星期几")
    private Integer weekDay;

    /**
     * 开始时间 (格式: HH:mm:ss 或 HH:mm)
     */
    @ExcelProperty(index = 6, value = "开始时间")
    private String startTime;

    /**
     * 结束时间 (格式: HH:mm:ss 或 HH:mm)
     */
    @ExcelProperty(index = 7, value = "结束时间")
    private String endTime;

    /**
     * 上课日期 (格式: yyyy-MM-dd)
     */
    @ExcelProperty(index = 8, value = "上课日期")
    private String courseDate;

    /**
     * 预到人数
     */
    @ExcelProperty(index = 9, value = "预到人数")
    private Integer expectedCount;

    /**
     * 学期（如：2024-2025-1）
     */
    @ExcelProperty(index = 10, value = "学期")
    private String semester;

    /**
     * 备注
     */
    @ExcelProperty(index = 11, value = "备注")
    private String remark;
}
