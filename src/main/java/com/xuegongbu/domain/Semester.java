package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学期实体类
 */
@Data
@TableName("semester")
@Schema(description = "学期")
public class Semester {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "雪花ID")
    private String id;

    @TableField("semester_name")
    @Schema(description = "学期名，例如：2024-2025学年春季学期")
    private String semesterName;

    @TableField("start_date")
    @Schema(description = "开始日期")
    private LocalDate startDate;

    @TableField("end_date")
    @Schema(description = "结束日期")
    private LocalDate endDate;

    @TableField("weeks")
    @Schema(description = "周数")
    private Integer weeks;

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