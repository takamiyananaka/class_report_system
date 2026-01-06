package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "学院管理员请求对象")
public class CollegeAdminRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "密码（创建时必填，更新时选填）")
    private String password;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @NotBlank(message = "所属学院ID不能为空")
    @Schema(description = "所属学院ID")
    private String collegeId;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
