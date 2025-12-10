package com.xuegongbu.domain;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
* 班级
* @TableName class
*/
@TableName(value = "class")
@Data
public class Class implements Serializable {

    /**
    * 主键ID
    */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
    * 班级名字
    */
    @NotBlank(message="[班级名字]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("班级名字")
    private String className;
    /**
    * 辅导员ID（教师ID）
    */
    @NotNull(message="[辅导员ID]不能为空")
    @ApiModelProperty("辅导员ID（教师ID）")
    private Long teacherId;
    /**
    * 班级人数
    */
    @NotNull(message="[班级人数]不能为空")
    @ApiModelProperty("班级人数")
    private Integer count;
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
    * 是否删除: 0-否, 1-是
    */
    @ApiModelProperty("是否删除: 0-否, 1-是")
    @TableLogic
    private Integer isDeleted;


}
