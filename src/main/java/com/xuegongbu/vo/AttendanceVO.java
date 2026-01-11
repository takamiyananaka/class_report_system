package com.xuegongbu.vo;

import com.xuegongbu.domain.Attendance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "考勤记录表vo对象")
public class AttendanceVO extends Attendance {
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
     * 任课老师
     */
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
     * 上课班级列表
     */
    private List<String> classNames;


}
