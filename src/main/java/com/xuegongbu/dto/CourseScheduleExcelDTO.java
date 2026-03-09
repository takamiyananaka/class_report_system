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
    @ExcelProperty(value = "课程号")
    private String courseNo;

    /**
     * 课程名称
     */
    @ExcelProperty(value = "课程名")
    private String courseName;

    /**
     * 课序号
     */
    @ExcelProperty(value = "课序号")
    private String orderNo;
    

    @ExcelProperty(value = "已排时间地点")
    private String schedule;
    
    /**
     * 上课班级列表（以,分割）
     */
    @ExcelProperty(value = "上课班级")
    private String classList;
    
    /**
     * 任课老师
     */
    @ExcelProperty(value = "上课教师")
    private String teacherName;
    
    /**
     * 课程类型（通识、专业课等）
     */
    @ExcelProperty(value = "课程类型")
    private String courseType;
    
    /**
     * 预到人数
     */
    @ExcelProperty(value = "选课人数")
    private String expectedCount;
    
    /**
     * 例如：2024-2025学年春季学期）
     */
    @ExcelProperty(value = "学期名")
    private String semesterName;
}
