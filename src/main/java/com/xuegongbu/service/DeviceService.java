package com.xuegongbu.service;

import java.util.List;
import java.util.Map;

public interface DeviceService {
    //批量获取设备url
    Map<String, Map<String, String>> getDeviceUrls(List<String> classroomNames) ;

    //获取单个设备url
    Map<String, String> getDeviceUrl(String classroomName) ;
}
