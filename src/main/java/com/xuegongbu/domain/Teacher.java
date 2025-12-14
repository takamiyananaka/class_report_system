package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 教师表
* @TableName teacher
*/
@TableName(value = "teacher")
@Data
public class Teacher implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
    * 用户名
    */
    @NotBlank(message="[用户名]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("用户名")
    @Size(max= 50,message="编码长度不能超过50")
    private String username;
    /**
    * 密码（BCrypt加密）
    */
    @NotBlank(message="[密码（BCrypt加密）]不能为空")
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("密码（BCrypt加密）")
    @Size(max= 255,message="编码长度不能超过255")
    private String password;
    /**
    * 真实姓名
    */
    @NotBlank(message="[真实姓名]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("真实姓名")
    @Size(max= 50,message="编码长度不能超过50")
    private String realName;
    /**
    * 教师工号
    */
    @NotBlank(message="[教师工号]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("教师工号")
    @Size(max= 50,message="编码长度不能超过50")
    private String teacherNo;
    /**
    * 手机号
    */
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("手机号")
    @Size(max= 20,message="编码长度不能超过20")
    private String phone;
    /**
    * 邮箱
    */
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("邮箱")
    @Size(max= 100,message="编码长度不能超过100")
    private String email;
    /**
    * 所属部门/学院
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("所属部门/学院")
    @Size(max= 255,message="编码长度不能超过255")
    private String department;
    /**
    * 状态：0-禁用，1-启用
    */
    @ApiModelProperty("状态：0-禁用，1-启用")
    private Integer status;
    /**
    * 最后登录时间
    */
    @ApiModelProperty("最后登录时间")
    private LocalDateTime lastLoginTime;
    /**
    * 最后登录IP
    */
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("最后登录IP")
    @Size(max= 50,message="编码长度不能超过50")
    private String lastLoginIp;
    /**
    * 备注
    */
    @Size(max= 500,message="编码长度不能超过500")
    @ApiModelProperty("备注")
    @Size(max= 500,message="编码长度不能超过500")
    private String remark;
    /**
    * 创建时间
    */
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
    * 更新时间
    */
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
    * 是否删除：0-否，1-是
    */
    @ApiModelProperty("是否删除：0-否，1-是")
    @TableLogic
    private Integer isDeleted;

}
