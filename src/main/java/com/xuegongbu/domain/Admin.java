package com.xuegongbu.domain;


import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 管理员表
* @TableName admin
*/
@TableName(value = "admin")
@Data
public class Admin implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    private Long id;
    /**
    * 用户名
    */
    @NotBlank(message="[用户名]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("用户名")
    @Length(max= 50,message="编码长度不能超过50")
    private String username;
    /**
    * 密码（BCrypt加密）
    */
    @NotBlank(message="[密码（BCrypt加密）]不能为空")
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("密码（BCrypt加密）")
    @Length(max= 255,message="编码长度不能超过255")
    private String password;
    /**
    * 真实姓名
    */
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("真实姓名")
    @Length(max= 50,message="编码长度不能超过50")
    private String realName;
    /**
    * 手机号
    */
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("手机号")
    @Length(max= 20,message="编码长度不能超过20")
    private String phone;
    /**
    * 邮箱
    */
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("邮箱")
    @Length(max= 100,message="编码长度不能超过100")
    private String email;
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
    @Length(max= 50,message="编码长度不能超过50")
    private String lastLoginIp;
    /**
    * 备注
    */
    @Size(max= 500,message="编码长度不能超过500")
    @ApiModelProperty("备注")
    @Length(max= 500,message="编码长度不能超过500")
    private String remark;
    /**
    * 创建时间
    */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    /**
    * 更新时间
    */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
    /**
    * 是否删除：0-否，1-是
    */
    @ApiModelProperty("是否删除：0-否，1-是")
    private Integer isDeleted;

}
