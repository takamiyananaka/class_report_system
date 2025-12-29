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
    @ExcelProperty(index = 0, value = "KCM")
    private String courseName;

    /**
     * 课程号
     */
    @ExcelProperty(index = 1, value = "KCH")
    private String courseNo;

    /**
     * 课序号
     */
    @ExcelProperty(index = 2, value = "KXH")
    private String orderNo;
    

    /**
     * 星期几（汉字）
     */
    @ExcelProperty(index = 3, value = "SKXQ")
    private String weekday;

    /**
     * 周次范围（格式：x-x周，例如：3-16周）
     */
    @ExcelProperty(index = 4, value = "ZCMC")
    private String weekRange;

    /**
     * 开始节次（1-12）
     */
    @ExcelProperty(index = 5, value = "KSJC")
    private Integer startPeriod;

    /**
     * 结束节次（1-12）
     */
    @ExcelProperty(index = 6, value = "JSJC")
    private Integer endPeriod;

    /**
     * 教室
     */
    @ExcelProperty(index = 7, value = "JASMC")
    private String classroom;
    
    /**
     * 上课班级列表（以,分割）
     */
    @ExcelProperty(index = 8, value = "WZSKBJ")
    private String classList;
}
