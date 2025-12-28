package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "课表查询请求参数,按班级")
public class CourseScheduleWithClassIdQueryDTO {
    @Schema(description = "班级id", example = "1")
    private String classId;
    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}
