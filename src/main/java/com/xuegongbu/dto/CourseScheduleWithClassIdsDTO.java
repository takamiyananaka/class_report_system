package com.xuegongbu.dto;

import com.xuegongbu.domain.CourseSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 课程安排与班级ID列表DTO
 */
@Data
@Schema(description = "课程安排与班级ID列表")
public class CourseScheduleWithClassIdsDTO {

    @Schema(description = "课程安排信息")
    private CourseSchedule courseSchedule;

    @Schema(description = "班级ID列表")
    private List<String> classIds;
}