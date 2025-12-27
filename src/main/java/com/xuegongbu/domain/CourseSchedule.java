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
* 课表表
* @TableName course_schedule
*/
@TableName(value = "course_schedule")
@Data
@Schema(description = "课表表")
public class CourseSchedule implements Serializable {

    /**
    * 主键ID
    */
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
    * 课程名称
    */
    @NotBlank(message="[课程名称]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @Schema(description = "课程名称")
    private String courseName;

    /**
    * 课程号
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "课程号")
    private String courseNo;

    /**
    * 课序号
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "课序号")
    private String orderNo;

    /**
    * 教师工号
    */
    @NotNull(message="[教师工号]不能为空")
    @Schema(description = "教师工号")
    private String teacherNo;

    /**
    * 班级名称
    */
    @NotBlank(message="[班级名称]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @Schema(description = "班级名称")
    private String className;
    
    /**
    * 星期几（1-7）
    */
    @NotNull(message="[星期几]不能为空")
    @Schema(description = "星期几（1-7）")
    private Integer weekday;
    
    /**
    * 周次范围（格式：x-x周，例如：3-16周）
    */
    @NotBlank(message="[周次范围]不能为空")
    @Size(max= 50,message="周次范围长度不能超过50")
    @Schema(description = "周次范围（格式：x-x周，例如：3-16周）")
    private String weekRange;
    
    /**
    * 开始节次（1-12）
    */
    @NotNull(message="[开始节次]不能为空")
    @Schema(description = "开始节次（1-12）")
    private Integer startPeriod;
    
    /**
    * 结束节次（1-12）
    */
    @NotNull(message="[结束节次]不能为空")
    @Schema(description = "结束节次（1-12）")
    private Integer endPeriod;
    
    /**
    * 教室
    */
    @NotBlank(message="[教室]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @Schema(description = "教室")
    private String classroom;

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

}