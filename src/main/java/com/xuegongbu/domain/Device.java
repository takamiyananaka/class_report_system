package com.xuegongbu.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 监控和教室名对应表
 */
@Data
public class Device {

    @JsonProperty("yardId")
    private String classroomId;
    @JsonProperty("yardName")
    private String classroomName;
    @JsonProperty("deviceId")
    private String deviceId;
}
