package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学院表
 * @TableName college
 */
@TableName(value = "college")
@Data
@Schema(description = "学院表")
public class College implements Serializable {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 学院名
     */
    @NotBlank(message="[学院名]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @Schema(description = "学院名")
    private String name;

    /**
     * 学院账号
     */
    @NotBlank(message="[学院账号]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "学院账号")
    private String username;

    /**
     * 学院密码（BCrypt加密）
     */
    @NotBlank(message="[学院密码]不能为空")
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "学院密码（BCrypt加密）")
    private String password;

    /**
     * 学院号
     */
    @NotBlank(message="[学院号]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "学院号")
    private String collegeNo;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private LocalDateTime loginTime;

    /**
     * 最后登录IP
     */
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "最后登录IP")
    private String loginIp;

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
