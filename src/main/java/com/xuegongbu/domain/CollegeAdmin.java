package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* 学院管理员表 - 用于登录认证
* @TableName college_admin
*/
@TableName(value = "college_admin")
@Data
@Schema(description = "学院管理员表")
public class CollegeAdmin implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
    * 学院管理员账号
    */
    @NotBlank(message="[学院管理员账号]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "学院管理员账号")
    private String username;

    /**
    * 学院管理员密码（BCrypt加密）
    */
    @NotBlank(message="[学院管理员密码]不能为空")
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "学院管理员密码（BCrypt加密）")
    private String password;

    /**
    * 真实姓名
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "真实姓名")
    private String realName;

    /**
    * 手机号
    */
    @Size(max= 20,message="编码长度不能超过20")
    @Schema(description = "手机号")
    private String phone;

    /**
    * 邮箱
    */
    @Size(max= 100,message="编码长度不能超过100")
    @Schema(description = "邮箱")
    private String email;

    /**
    * 所属学院ID
    */
    @NotBlank(message="[所属学院ID]不能为空")
    @Schema(description = "所属学院ID")
    private String collegeId;

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
    private String lastLoginIp;

    /**
    * 创建时间
    */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
    * 更新时间
    */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
    * 是否删除：0-否，1-是
    */
    @Schema(description = "是否删除：0-否，1-是")
    @TableLogic
    private Integer isDeleted;
}