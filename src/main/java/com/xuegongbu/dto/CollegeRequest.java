package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 学院请求DTO
 */
@Data
@Schema(description = "学院请求")
public class CollegeRequest implements Serializable {

    /**
     * 学院名
     */
    @NotBlank(message="[学院名]不能为空")
    @Size(max= 100, message="学院名长度不能超过100")
    @Schema(description = "学院名")
    private String name;

    /**
     * 学院号
     */
    @NotBlank(message="[学院号]不能为空")
    @Size(max= 50, message="学院号长度不能超过50")
    @Schema(description = "学院号")
    private String collegeNo;

    /**
     * 学院描述
     */
    @Size(max= 500,message="编码长度不能超过500")
    @Schema(description = "学院描述")
    private String description;
}
