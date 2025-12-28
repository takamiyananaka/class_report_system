package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学院信息表 - 仅包含学院基本信息，不含登录认证信息
 * @TableName college
 */
@TableName(value = "college")
@Data
@Schema(description = "学院信息表")
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
     * 学院号
     */
    @NotBlank(message="[学院号]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "学院号")
    private String collegeNo;

    /**
     * 学院描述
     */
    @Size(max= 500,message="编码长度不能超过500")
    @Schema(description = "学院描述")
    private String description;

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
