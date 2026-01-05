package com.xuegongbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加课程请求DTO
 * 与Excel批量导入模板字段完全一致
 */
@Data
@Schema(description = "添加课程请求")
public class CourseScheduleAddRequest implements Serializable {

    /**
     * 课程号
     */
    @Schema(description = "课程号")
    private String kch;

    /**
     * 课程名称
     */
    @Schema(description = "课程名称")
    private String kcm;

    /**
     * 课序号
     */
    @Schema(description = "课序号")
    private String kxh;

    /**
     * 周次范围（格式：x-x周，例如：3-16周）
     */
    @Schema(description = "上课周次（格式：x-x周）")
    private String zcmc;

    /**
     * 星期几（汉字：星期一至星期日）
     */
    @Schema(description = "上课星期（汉字：星期一至星期日）")
    private String skxq;

    /**
     * 开始节次（1-12）
     */
    @Schema(description = "开始节次（1-12）")
    private Integer ksjc;

    /**
     * 结束节次（1-12）
     */
    @Schema(description = "结束节次（1-12）")
    private Integer jsjc;

    /**
     * 教室
     */
    @Schema(description = "上课教室名")
    private String jasmc;

    /**
     * 上课班级列表（以英文逗号分割）
     */
    @Schema(description = "上课班级（多个班级用英文逗号分隔）")
    private String wzskbj;

    /**
     * 任课老师
     */
    @Schema(description = "任课老师")
    private String rkls;

    /**
     * 课程类型（通识、专业课等）
     */
    @Schema(description = "课程类型")
    private String kclx;

    /**
     * 预到人数
     */
    @Schema(description = "预到人数")
    private Integer ydrs;
}
