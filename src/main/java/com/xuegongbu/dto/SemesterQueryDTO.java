package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学期查询DTO
 */
@Data
@Schema(description = "学期查询DTO")
public class SemesterQueryDTO {

    @Schema(description = "学期名")
    private String semesterName;

    @Schema(description = "页码，从1开始")
    private Integer pageNum = 1;

    @Schema(description = "每页大小")
    private Integer pageSize = 10;
}