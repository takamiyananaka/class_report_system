package com.xuegongbu.domain;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
* 管理员表
* @TableName admin
*/
@TableName(value = "admin")
@Data
@Schema(description = "管理员表")
public class Admin implements Serializable {

    /**
    * 主键ID
    */
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
    * 用户名
    */
    @NotBlank(message="[用户名]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "用户名")
    @Size(max= 50,message="编码长度不能超过50")
    private String username;
    /**
    * 密码（BCrypt加密）
    */
    @NotBlank(message="[密码（BCrypt加密）]不能为空")
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "密码（BCrypt加密）")
    @Size(max= 255,message="编码长度不能超过255")
    private String password;
    /**
    * 真实姓名
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "真实姓名")
    @Size(max= 50,message="编码长度不能超过50")
    private String realName;
    /**
    * 手机号
    */
    @Size(max= 20,message="编码长度不能超过20")
    @Schema(description = "手机号")
    @Size(max= 20,message="编码长度不能超过20")
    private String phone;
    /**
    * 邮箱
    */
    @Size(max= 100,message="编码长度不能超过100")
    @Schema(description = "邮箱")
    @Size(max= 100,message="编码长度不能超过100")
    private String email;
    /**
    * 状态：0-禁用，1-启用
    */
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
    /**
    * 最后登录时间
    */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
    /**
    * 最后登录IP
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "最后登录IP")
    @Size(max= 50,message="编码长度不能超过50")
    private String lastLoginIp;
    /**
    * 备注
    */
    @Size(max= 500,message="编码长度不能超过500")
    @Schema(description = "备注")
    @Size(max= 500,message="编码长度不能超过500")
    private String remark;
    /**
    * 创建时间
    */
    @Schema(description = "创建时间")
    @com.baomidou.mybatisplus.annotation.TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
    * 更新时间
    */
    @Schema(description = "更新时间")
    @com.baomidou.mybatisplus.annotation.TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
    * 是否删除：0-否，1-是
    */
    @Schema(description = "是否删除：0-否，1-是")
    @com.baomidou.mybatisplus.annotation.TableLogic
    private Integer isDeleted;

}