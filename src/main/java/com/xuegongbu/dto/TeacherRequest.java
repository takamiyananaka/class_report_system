package com.xuegongbu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    private String password;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @NotBlank(message = "教师工号不能为空")
    private String teacherNo;
    
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String department;
    
    /**
     * 身份字段：1=只是教师，2=教师且是辅导员
     */
    @Min(value = 1, message = "身份字段值无效，只能为1（教师）或2（辅导员）")
    @Max(value = 2, message = "身份字段值无效，只能为1（教师）或2（辅导员）")
    private Integer identity;
    
    private Integer status;
}
