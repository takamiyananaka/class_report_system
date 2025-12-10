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
    @ExcelProperty(index = 0, value = "班级名称")
    private String className;

    /**
     * 辅导员工号
     */
    @ExcelProperty(index = 1, value = "辅导员工号")
    private String teacherNo;

    /**
     * 班级人数
     */
    @ExcelProperty(index = 2, value = "班级人数")
    private Integer count;
}
