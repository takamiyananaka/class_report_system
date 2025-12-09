package com.xuegongbu.domain;

import lombok.Data;

/**
 * 监控和教室名对应表
 */
@Data
public class Device {

    private String classroomId;

    private String classroomName;

    private String deviceId;
}
