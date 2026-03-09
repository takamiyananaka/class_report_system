package com.xuegongbu.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 教师Excel导入DTO
 */
@Data
@ColumnWidth(25)
public class TeacherExcelDTO {

    @ExcelProperty(value = "辅导员工号")
    @NotBlank(message = "工号不能为空")
    private String teacherNo;

    @ExcelProperty(value = "辅导员姓名")
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @ExcelProperty(value = "院系")
    @NotBlank(message = "学院名不能为空")
    private String collegeName;
}