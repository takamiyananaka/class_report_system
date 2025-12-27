package com.xuegongbu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学院视图对象
 */
@Data
@Schema(description = "学院视图对象")
public class CollegeVO implements Serializable {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private String id;

    /**
     * 学院名
     */
    @Schema(description = "学院名")
    private String name;

    /**
     * 学院账号
     */
    @Schema(description = "学院账号")
    private String username;

    /**
     * 学院号
     */
    @Schema(description = "学院号")
    private String collegeNo;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private LocalDateTime loginTime;

    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP")
    private String loginIp;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
