package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* 图片抓取记录表
* @TableName image_capture
*/
@TableName(value = "image_capture")
@Data
@Schema(description = "图片抓取记录表")
public class ImageCapture implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
    * 来源URL
    */
    @NotBlank(message="[来源URL]不能为空")
    @Size(max= 500,message="编码长度不能超过500")
    @Schema(description = "来源URL")
    @Size(max= 500,message="编码长度不能超过500")
    private String sourceUrl;
    /**
    * 保存的图片URL
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "保存的图片URL")
    @Size(max= 255,message="编码长度不能超过255")
    private String imageUrl;
    /**
    * 教室号
    */
    @Size(max= 50,message="编码长度不能超过50")
    @Schema(description = "教室号")
    @Size(max= 50,message="编码长度不能超过50")
    private String classroom;
    /**
    * 抓取时间
    */
    @NotNull(message="[抓取时间]不能为空")
    @Schema(description = "抓取时间")
    private LocalDateTime captureTime;
    /**
    * 识别人数
    */
    @Schema(description = "识别人数")
    private Integer personCount;
    /**
    * 识别状态：0-未识别，1-识别成功，2-识别失败
    */
    @Schema(description = "识别状态：0-未识别，1-识别成功，2-识别失败")
    private Integer recognitionStatus;
    /**
    * 错误信息
    */
    @Size(max= 500,message="编码长度不能超过500")
    @Schema(description = "错误信息")
    @Size(max= 500,message="编码长度不能超过500")
    private String errorMessage;
    /**
    * 创建时间
    */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


}