package com.xuegongbu.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
@Service
public interface DeviceService {
    //批量获取设备url
    Map<String, Map<String, String>> getDeviceUrls(List<String> classroomNames) ;

    //获取单个设备url
    Map<String, String> getDeviceUrl(String classroomName) ;
}
