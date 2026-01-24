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
    * 课程名称
    */
    @NotBlank(message="[课程名称]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @Schema(description = "课程名称")

    private String courseName;

    /**
     * 星期几（汉字：星期一至星期日）
     */
    @NotBlank(message="[星期几]不能为空")
    @Size(max= 10,message="编码长度不能超过10")
    @Schema(description = "星期几（汉字：星期一至星期日）")
    private String weekday;
    
    /**
    * 预到人数
    */
    @Schema(description = "预到人数")
    private Integer expectedCount;
    
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
     * 任课老师
     */
    @Size(max = 100, message = "任课老师名称长度不能超过100")
    @Schema(description = "任课老师")
    private String teacherName;

    /**
     * 课程类型（通识、专业课等）
     */
    @Size(max = 50, message = "课程类型长度不能超过50")
    @Schema(description = "课程类型（通识、专业课等）")
    private String courseType;

    /**
     * 学期名，例如：2024-2025学年春季学期
     */
    @Size(max = 100, message = "学期名长度不能超过100")
    @Schema(description = "学期名，例如：2024-2025学年春季学期")
    private String semesterName;

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