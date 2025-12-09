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

    private Integer status;
}
