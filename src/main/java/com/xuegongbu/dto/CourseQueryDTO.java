package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程班级关联查询请求DTO
 */
@Data
@Schema(description = "课程班级关联查询请求参数")
public class CourseQueryDTO {

    @Schema(description = "课程ID", example = "1234567890")
    private String courseId;

    @Schema(description = "班级ID", example = "9876543210")
    private String classId;

    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}