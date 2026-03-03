package com.xuegongbu.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 班级Excel导入DTO
 */
@Data
public class ClassExcelDTO implements Serializable {

    /**
     * 班级名字
     */
    @ExcelProperty(value = "班级名称")
    private String className;

    /**
     * 班级人数
     */
    @ExcelProperty(value = "总人数")
    private Integer count;

    /**
     * 年级
     */
    @ExcelProperty(value = "年级")
    private String grade;

    /**
     * 专业
     */
    @ExcelProperty(value = "专业")
    private String major;

    /**
     * 辅导员工号
     */
    @ExcelProperty(value = "辅导员工号")
    private String teacherNo;
}
