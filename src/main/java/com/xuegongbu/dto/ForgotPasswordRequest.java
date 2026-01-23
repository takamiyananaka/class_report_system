package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 忘记密码请求DTO
 */
@Data
@Schema(description = "忘记密码请求")
public class ForgotPasswordRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "用户邮箱，发送验证码的接口只需要填这个字段")
    private String email;

    @Schema(description = "验证码")
    private String verificationCode;

    @Schema(description = "新密码")
    private String newPassword;
}