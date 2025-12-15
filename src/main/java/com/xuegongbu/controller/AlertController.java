package com.xuegongbu.controller;

import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Alert;
import com.xuegongbu.service.AlertService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/alert")
@Tag(name = "预警管理", description = "预警相关接口")
public class AlertController {
    
    @Autowired
    private AlertService alertService;

    /**
    * 根据老师id获取预警记录
    */
    @GetMapping("/listByTeacherId")
    @Operation(summary = "根据老师id获取预d警记录", description = "根据老师id获取预警记录")
    public Result<List<Alert>> listByTeacherId(){
        log.info("开始执行获取预警记录任务");
        
        // 从安全上下文中获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("用户未认证");
        }
        
        // 获取教师ID（从JWT token中解析出来的teacherNo）
        Object principal = authentication.getPrincipal();
        Long teacherId = null;
        if (principal instanceof String) {
            try {
                teacherId = Long.valueOf((String) principal);
            } catch (NumberFormatException e) {
                return Result.error("无法解析教师ID");
            }
        } else if (principal instanceof Long) {
            teacherId = (Long) principal;
        } else {
            return Result.error("用户身份信息格式错误");
        }
        
        List<Alert> alertList = alertService.listByTeacherId(teacherId);
        log.info("结束执行获取预警记录任务");
        return Result.success(alertList);
    }
}