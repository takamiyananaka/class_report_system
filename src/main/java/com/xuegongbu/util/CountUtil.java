package com.xuegongbu.util;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.xuegongbu.dto.CountResponse;
import com.xuegongbu.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CountUtil {

    private final ImageService imageService;

    public CountUtil(ImageService imageService) {
        this.imageService = imageService;
    }

    public CountResponse getCount(Map<String, String> deviceUrls) {
        //构建http请求
        /**
         * POST http://localhost:8000/count
         * Content-Type: application/json
         *
         * {
         *   "source": "https://ddxk.swpu.edu.cn:8063/live/phc1_1247505953717374976.live.flv",
         *   "weights": "weights/yolov10l.pt",
         *   "imgsz": 1280,
         *   "conf": 0.25,
         *   "max_frames": 10,
         *   "person_only": true,
         *   "save_samples": 1,
         *   "device_id": "1247505953717374976"
         * }
         */
        String url = "http://localhost:8082/count";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("source", deviceUrls.get("highChn2"));
        requestBody.put("weights", "weights/yolov10l.pt");
        requestBody.put("imgsz", 1280);
        requestBody.put("conf", 0.25);
        requestBody.put("max_frames", 10);
        requestBody.put("person_only", true);
        requestBody.put("save_samples", 1);
        requestBody.put("device_id", deviceUrls.get("device_id"));
        
        String json = JSONUtil.toJsonStr(requestBody);
        HttpResponse response = HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .body(json)
                .execute();
        String body = response.body();
        System.out.println(body);
        CountResponse countResponse = JSONUtil.toBean(response.body(), CountResponse.class);
        // 上传图片并获取URL
        String imageUrl = imageService.getImageUrl(countResponse.getSampleUrl());
        countResponse.setSampleUrl(imageUrl);
        return countResponse;
    }
}