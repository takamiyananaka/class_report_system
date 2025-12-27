package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* 课程班级关联表（course表作为关联表）
* @TableName course
*/
@TableName(value = "course")
@Data
@Schema(description = "课程班级关联表")
public class Course implements Serializable {

    /**
    * 主键ID（雪花算法生成）
    */
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
    * 课程ID
    */
    @NotNull(message="[课程ID]不能为空")
    @Schema(description = "课程ID")
    private String courseId;

    /**
    * 班级ID
    */
    @NotNull(message="[班级ID]不能为空")
    @Schema(description = "班级ID")
    private String classId;

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
    private Integer isDelete;

}