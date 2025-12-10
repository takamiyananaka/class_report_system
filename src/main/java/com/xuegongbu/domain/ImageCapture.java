package com.xuegongbu.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 图片抓取记录表
* @TableName image_capture
*/
@TableName(value = "image_capture")
@Data
public class ImageCapture implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
    * 来源URL
    */
    @NotBlank(message="[来源URL]不能为空")
    @Size(max= 500,message="编码长度不能超过500")
    @ApiModelProperty("来源URL")
    @Size(max= 500,message="编码长度不能超过500")
    private String sourceUrl;
    /**
    * 保存的图片URL
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("保存的图片URL")
    @Size(max= 255,message="编码长度不能超过255")
    private String imageUrl;
    /**
    * 教室号
    */
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("教室号")
    @Size(max= 50,message="编码长度不能超过50")
    private String classroom;
    /**
    * 抓取时间
    */
    @NotNull(message="[抓取时间]不能为空")
    @ApiModelProperty("抓取时间")
    private LocalDateTime captureTime;
    /**
    * 识别人数
    */
    @ApiModelProperty("识别人数")
    private Integer personCount;
    /**
    * 识别状态：0-未识别，1-识别成功，2-识别失败
    */
    @ApiModelProperty("识别状态：0-未识别，1-识别成功，2-识别失败")
    private Integer recognitionStatus;
    /**
    * 错误信息
    */
    @Size(max= 500,message="编码长度不能超过500")
    @ApiModelProperty("错误信息")
    @Size(max= 500,message="编码长度不能超过500")
    private String errorMessage;
    /**
    * 创建时间
    */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;


}
