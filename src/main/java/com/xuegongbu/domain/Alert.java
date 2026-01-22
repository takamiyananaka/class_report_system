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
* 预警记录表
* @TableName alert
*/
@TableName(value = "alert")
@Data
@Schema(description = "预警记录表")
public class Alert implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
    * 课程ID
    */
    @NotNull(message="[课程ID]不能为空")
    @Schema(description = "课程ID")
    private String courseId;
    

    /**
    * 班级ID
    */
    @NotBlank(message="[班级ID]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @Schema(description = "班级ID")
    private String classId;
    
    /**
    * 考勤记录ID
    */
    @Schema(description = "考勤记录ID")
    private String attendanceId;
    
    /**
    * 预警类型：1-人数不足，2-迟到过多，3-旷课严重
    */
    @NotNull(message="[预警类型]不能为空")
    @Schema(description = "预警类型：1-人数不足，2-迟到过多，3-旷课严重")
    private Integer alertType;
    
    /**
    * 预警级别：1-低，2-中，3-高
    */
    @Schema(description = "预警级别：1-低，2-中，3-高")
    private Integer alertLevel;
    
    /**
    * 预到人数
    */
    @Schema(description = "预到人数")
    private Integer expectedCount;
    
    /**
    * 实到人数
    */
    @Schema(description = "实到人数")
    private Integer actualCount;
    
    /**
    * 预警信息
    */
    @Size(max= 500,message="编码长度不能超过500")
    @Schema(description = "预警信息")
    @Size(max= 500,message="编码长度不能超过500")
    private String alertMessage;
    
    /**
    * 通知状态：0-未发送，1-已发送，2-发送失败
    */
    @Schema(description = "通知状态：0-未发送，1-已发送，2-发送失败")
    private Integer notifyStatus;

    /**
    * 阅读状态：0-未读，1-已读
    */
    @Schema(description = "阅读状态：0-未读，1-已读")
    private Integer readStatus;
    
    /**
    * 通知时间
    */
    @Schema(description = "通知时间")
    private LocalDateTime notifyTime;
    
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