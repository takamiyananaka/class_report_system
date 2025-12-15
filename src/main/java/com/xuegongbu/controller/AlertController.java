package com.xuegongbu.controller;

import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Alert;
import com.xuegongbu.service.AlertService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/alert")
@Tag(name = "预警管理", description = "预警相关接口")
public class AlertController {
    
    @Autowired
    private AlertService alertService;
}