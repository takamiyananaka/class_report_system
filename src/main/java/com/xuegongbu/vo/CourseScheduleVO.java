package com.xuegongbu.vo;

import com.xuegongbu.domain.CourseSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "课表VO对象")
public class CourseScheduleVO extends CourseSchedule {
    @Schema(description = "班级名称列表")
    private List<String> classNames;
    @Schema(description = "是否在上课时间")
    private Boolean inClassTime;
}
