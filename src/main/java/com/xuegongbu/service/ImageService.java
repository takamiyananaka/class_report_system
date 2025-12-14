package com.xuegongbu.service;

import org.springframework.stereotype.Service;

@Service
public interface ImageService {
    String getImageUrl(String imagePath);
}