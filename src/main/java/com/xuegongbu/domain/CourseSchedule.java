package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 课表表
* @TableName course_schedule
*/
@TableName(value = "course_schedule")
@Data
public class CourseSchedule implements Serializable {

    /**
    * 主键ID
    */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
    * 课程名称
    */
    @NotBlank(message="[课程名称]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("课程名称")
    private String courseName;

    /**
    * 教师ID
    */
    @NotNull(message="[教师ID]不能为空")
    @ApiModelProperty("教师ID")
    private Long teacherId;

    /**
    * 班级名称
    */
    @NotBlank(message="[班级名称]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("班级名称")
    private String className;
    
    /**
    * 星期几（1-7）
    */
    @NotNull(message="[星期几]不能为空")
    @ApiModelProperty("星期几（1-7）")
    private Integer weekday;
    
    /**
    * 开始时间
    */
    @NotNull(message="[开始时间]不能为空")
    @ApiModelProperty("开始时间")
    private LocalTime startTime;
    
    /**
    * 结束时间
    */
    @NotNull(message="[结束时间]不能为空")
    @ApiModelProperty("结束时间")
    private LocalTime endTime;
    
    /**
    * 教室
    */
    @NotBlank(message="[教室]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("教室")
    private String classroom;
    
    /**
    * 学期
    */
    @NotBlank(message="[学期]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("学期")
    private String semester;

    /**
    * 学年
    */
    @NotBlank(message="[学年]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("学年")
    private String schoolYear;

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

}
