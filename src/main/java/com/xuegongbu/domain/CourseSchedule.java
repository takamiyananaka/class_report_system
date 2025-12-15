package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalTime;
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
    * 开始时间
    */
    @NotNull(message="[开始时间]不能为空")
    @Schema(description = "开始时间")
    private LocalTime startTime;
    
    /**
    * 结束时间
    */
    @NotNull(message="[结束时间]不能为空")
    @Schema(description = "结束时间")
    private LocalTime endTime;
    
    /**
    * 教室
    */
    @NotBlank(message="[教室]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @Schema(description = "教室")
    private String classroom;
    
    /**
    * 学期
    */
    @NotBlank(message="[学期]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "学期")
    private String semester;

    /**
    * 学年
    */
    @NotBlank(message="[学年]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @Schema(description = "学年")
    private String schoolYear;

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