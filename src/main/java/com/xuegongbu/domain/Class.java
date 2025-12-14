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
    * id
    */
    @NotNull(message="[id]不能为空")
    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
    * 班级名字
    */
    @NotBlank(message="[班级名字]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("班级名字")
    @Size(max= 20,message="编码长度不能超过20")
    private String className;
    /**
    * 辅导员工号
    */
    @NotBlank(message="[辅导员工号]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("辅导员工号")
    @Size(max= 50,message="编码长度不能超过50")
    private String teacherNo;
    /**
    * 班级人数
    */
    @NotNull(message="[班级人数]不能为空")
    @ApiModelProperty("班级人数")
    private Integer count;
    /**
    * 年级
    */
    @NotBlank(message="[年级]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("年级")
    private String grade;
    /**
    * 专业
    */
    @NotBlank(message="[专业]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("专业")
    private String major;
    /**
    * 创建时间
    */
    @ApiModelProperty("创建时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
    * 更新时间
    */
    @ApiModelProperty("更新时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
    * 是否删除: 0-否, 1-是
    */
    @NotNull(message="[是否删除: 0-否, 1-是]不能为空")
    @ApiModelProperty("是否删除: 0-否, 1-是")
    @TableLogic
    private Integer isDelete;


}
