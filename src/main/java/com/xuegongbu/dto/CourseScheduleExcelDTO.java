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
     * 课程号
     */
    @ExcelProperty(value = "KCH")
    private String courseNo;

    /**
     * 课程名称
     */
    @ExcelProperty(value = "KCM")
    private String courseName;

    /**
     * 课序号
     */
    @ExcelProperty(value = "KXH")
    private String orderNo;
    
    /**
     * 周次范围（格式：x-x周，例如：3-16周）
     */
    @ExcelProperty(value = "ZCMC")
    private String weekRange;

    /**
     * 星期几（汉字）
     */
    @ExcelProperty(value = "SKXQ")
    private String weekday;

    /**
     * 开始节次（1-12）
     */
    @ExcelProperty(value = "KSJC")
    private String startPeriod;

    /**
     * 结束节次（1-12）
     */
    @ExcelProperty(value = "JSJC")
    private String endPeriod;

    /**
     * 教室
     */
    @ExcelProperty(value = "JASMC")
    private String classroom;
    
    /**
     * 上课班级列表（以,分割）
     */
    @ExcelProperty(value = "WZSKBJ")
    private String classList;
    
    /**
     * 任课老师
     */
    @ExcelProperty(value = "RKLS")
    private String teacherName;
    
    /**
     * 课程类型（通识、专业课等）
     */
    @ExcelProperty(value = "KCLX")
    private String courseType;
    
    /**
     * 预到人数
     */
    @ExcelProperty(value = "YDRS")
    private String expectedCount;
    
    /**
     * 学期名（例如：2024-2025学年春季学期）
     */
    @ExcelProperty(value = "XQ")
    private String semesterName;
}
