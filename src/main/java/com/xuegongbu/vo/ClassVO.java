package com.xuegongbu.vo;

import com.xuegongbu.domain.Class;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "班级VO对象")
public class ClassVO extends Class {
    @Schema(description = "辅导员名字")
    private String teacherName;
    @Schema(description = "所属学院")
    private String collegeName;
}
