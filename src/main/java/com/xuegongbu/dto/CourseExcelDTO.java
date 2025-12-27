package com.xuegongbu.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 课程班级关联Excel导入DTO
 */
@Data
public class CourseExcelDTO implements Serializable {

    /**
     * 课程ID
     */
    @ExcelProperty(index = 0, value = "课程ID")
    private String courseId;

    /**
     * 班级ID
     */
    @ExcelProperty(index = 1, value = "班级ID")
    private String classId;
}
