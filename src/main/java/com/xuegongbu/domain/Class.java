package com.xuegongbu.domain;


import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

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
    private Integer id;
    /**
    * 班级名字
    */
    @NotBlank(message="[班级名字]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("班级名字")
    @Length(max= 20,message="编码长度不能超过20")
    private String class_name;
    /**
    * 辅导员id
    */
    @NotBlank(message="[辅导员id]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("辅导员id")
    @Length(max= 50,message="编码长度不能超过50")
    private String teacher_id;
    /**
    * 班级人数
    */
    @NotNull(message="[班级人数]不能为空")
    @ApiModelProperty("班级人数")
    private Integer count;
    /**
    * 创建时间
    */
    @NotNull(message="[创建时间]不能为空")
    @ApiModelProperty("创建时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime create_time;
    /**
    * 更新时间
    */
    @ApiModelProperty("更新时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime update_time;
    /**
    * 是否删除: 0-否, 1-是
    */
    @NotNull(message="[是否删除: 0-否, 1-是]不能为空")
    @ApiModelProperty("是否删除: 0-否, 1-是")
    @TableLogic
    private Integer is_delete;


}
