package com.xuegongbu.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuegongbu.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {
    
    @Override
    public String getImageUrl(String imagePath) {
        try {
            // 登录获取Token
            String token = login();
            if (token == null) {
                log.error("获取Token失败");
                return null;
            }

            // 加上handoff_backend文件夹相对于ImgServiceImpl的相对路径前缀
            String relativePath = "handoff_backend" + File.separator + imagePath;
            
            // 上传图片获取URL
            String imageUrl = uploadImage(token, relativePath);
            return imageUrl;
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return null;
        }
    }
    
    /**
     * 登录并获取Token
     */
    private String login() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://117.72.173.242:8082/api/v1/tokens";
        
        // 构造请求体
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", "admin@admin.com"); // 需要替换为实际邮箱
        formData.add("password", "123456"); // 需要替换为实际密码
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode dataNode = rootNode.get("data");
            if (dataNode != null && dataNode.has("token")) {
                return dataNode.get("token").asText();
            }
        }
        return null;
    }
    
    /**
     * 上传图片并获取URL
     */
    private String uploadImage(String token, String imagePath) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://117.72.173.242:8082/api/v1/upload";
        
        // 准备文件
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            log.error("图片文件不存在: {}", imagePath);
            return null;
        }
        
        // 构造multipart请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.FileSystemResource(imageFile));
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode dataNode = rootNode.get("data");
            if (dataNode != null && dataNode.has("links")) {
                JsonNode linksNode = dataNode.get("links");
                if (linksNode.has("url")) {
                    return linksNode.get("url").asText();
                }
            }
        }
        return null;
    }
}