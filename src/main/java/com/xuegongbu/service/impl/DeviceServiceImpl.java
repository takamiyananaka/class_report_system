package com.xuegongbu.service.impl;

import com.xuegongbu.domain.Device;
import com.xuegongbu.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class DeviceServiceImpl implements DeviceService {

    private final RedisTemplate<String, Object> redisTemplate;

    public DeviceServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate =redisTemplate;
    }


    //批量获取设备url
    @Override
    public Map<String, Map<String, String>> getDeviceUrls(List<String> classroomNames)  {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (String classroomName : classroomNames) {
            Map<String, String> urls = getDeviceUrl(classroomName);
            result.put(classroomName, urls);
        }
        return result;
    }

    //获取单个设备url
    @Override
    public Map<String, String> getDeviceUrl(String classroomName)  {
        //redis读取
        Device device = (Device) redisTemplate.opsForValue().get(classroomName);
        String deviceId = device.getDeviceId();
        String baseUrl1 = "https://ddxk.swpu.edu.cn:8063/live/";
        String baseUrl2 = "https://ddxk.swpu.edu.cn:8064/live/";
        String urlSuffix = ".live.flv";
        return Arrays.stream(new Object[][]{
                        {"deviceModel","HH" },
                        {"highChn0",baseUrl2+"phc0_"+deviceId+urlSuffix},
                        {"highChn1",baseUrl1+"phc1_"+deviceId+urlSuffix },
                        {"highChn2",baseUrl2+"phc2_"+deviceId+urlSuffix },
                        {"device_id",deviceId}
                })
                .collect(HashMap::new, (map, entry) -> map.put((String) entry[0], (String) entry[1]), HashMap::putAll);
    }
}
