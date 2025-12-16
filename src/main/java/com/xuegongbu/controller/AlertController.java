package com.xuegongbu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Alert;
import com.xuegongbu.dto.AlertQueryDTO;
import com.xuegongbu.service.AlertService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.session.SaSession;

@Slf4j
@RestController
@RequestMapping("/alert")
@Tag(name = "预警管理", description = "预警相关接口")
public class AlertController {
    
    @Autowired
    private AlertService alertService;

    /**
    * 根据老师工号获取预警记录（分页，支持时间范围查询）
    */
    @PostMapping("/getAlertList")
    @Operation(summary = "分页条件查询预警列表", description = "分页条件查询预警列表")
    public Result<Page<Alert>> getAlertList(AlertQueryDTO queryDTO){
        log.info("开始执行获取预警记录任务");
        
        // 从Sa-Token中获取当前用户信息
        if (!StpUtil.isLogin()) {
            return Result.error("用户未认证");
        }

        // 优先从会话中获取完整的用户信息
        String teacherNo = null;
        try {
            SaSession session = StpUtil.getSession();
            com.xuegongbu.domain.Teacher teacher = (com.xuegongbu.domain.Teacher) session.get("userInfo");
            if (teacher != null) {
                teacherNo = teacher.getTeacherNo();
            } else {
                Object loginId = StpUtil.getLoginId();
                if (loginId instanceof String) {
                    teacherNo = (String) loginId;
                }
            }
        } catch (Exception e) {
            log.error("无法解析当前登录教师工号: {}", e.getMessage());
            return Result.error("用户身份信息格式错误");
        }
        
        if (teacherNo == null) {
            return Result.error("无法获取教师工号");
        }
        
        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() <= 0) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() <= 0) {
            queryDTO.setPageSize(10);
        }
        
        Page<Alert> alertPage = alertService.getAlertList(queryDTO, teacherNo);
        return Result.success(alertPage);
    }
}