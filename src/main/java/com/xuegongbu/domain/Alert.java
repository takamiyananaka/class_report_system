package com.xuegongbu.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 预警记录表
* @TableName alert
*/
@TableName(value = "alert")
@Data
public class Alert implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    private Long id;
    /**
    * 课程ID
    */
    @NotNull(message="[课程ID]不能为空")
    @ApiModelProperty("课程ID")
    private Long courseId;
    /**
    * 考勤记录ID
    */
    @ApiModelProperty("考勤记录ID")
    private Long attendanceId;
    /**
    * 预警类型：1-人数不足，2-迟到过多，3-旷课严重
    */
    @NotNull(message="[预警类型：1-人数不足，2-迟到过多，3-旷课严重]不能为空")
    @ApiModelProperty("预警类型：1-人数不足，2-迟到过多，3-旷课严重")
    private Integer alertType;
    /**
    * 预警级别：1-低，2-中，3-高
    */
    @ApiModelProperty("预警级别：1-低，2-中，3-高")
    private Integer alertLevel;
    /**
    * 预到人数
    */
    @ApiModelProperty("预到人数")
    private Integer expectedCount;
    /**
    * 实到人数
    */
    @ApiModelProperty("实到人数")
    private Integer actualCount;
    /**
    * 预警信息
    */
    @Size(max= 500,message="编码长度不能超过500")
    @ApiModelProperty("预警信息")
    @Size(max= 500,message="编码长度不能超过500")
    private String alertMessage;
    /**
    * 通知状态：0-未发送，1-已发送，2-发送失败
    */
    @ApiModelProperty("通知状态：0-未发送，1-已发送，2-发送失败")
    private Integer notifyStatus;
    /**
    * 通知时间
    */
    @ApiModelProperty("通知时间")
    private LocalDateTime notifyTime;
    /**
    * 创建时间
    */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    /**
    * 更新时间
    */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
