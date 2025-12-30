package com.xuegongbu.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Alert;
import com.xuegongbu.dto.AlertQueryDTO;
import com.xuegongbu.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.stp.StpUtil;

import java.util.List;

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
    @SaCheckRole("teacher")
    public Result<Page<Alert>> getAlertList(@RequestBody AlertQueryDTO queryDTO){
        log.info("开始执行获取预警记录任务");
        
        // 从Sa-Token中获取当前用户信息
        if (!StpUtil.isLogin()) {
            return Result.error("用户未认证");
        }

        String teacherNo = null;
        try {
            Object loginId = StpUtil.getLoginId();
            if (loginId instanceof String) {
                teacherNo = (String) loginId;
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

    /**
    * 批量删除预警记录
    */
    @DeleteMapping("/deleteAlerts")
    @Operation(summary = "批量删除预警记录", description = "批量删除预警记录")
    @SaCheckRole("teacher")
    public Result<String> deleteAlerts(@RequestBody List<String> ids){
        log.info("开始执行批量删除预警记录任务");
        Result<String> result = alertService.removeByIds(ids) ? Result.success("删除成功") : Result.error("删除失败");
        log.info("批量删除预警记录任务完成");
        return result;
    }

    /**
    * 修改阅读状态
    */
    @PutMapping("/updateAlertReadStatus")
    @Operation(summary = "修改阅读状态", description = "修改阅读状态")
    @SaCheckRole("teacher")
    public Result<String> updateAlertReadStatus(@RequestParam String  id){
        log.info("开始执行修改阅读状态任务,id: {}", id);
        Alert alert = alertService.getById(id);
        if (alert == null) {
            return Result.error("记录不存在");
        }
        alert.setReadStatus(1);
        Result<String> result = alertService.updateById(alert) ? Result.success("修改成功") : Result.error("修改失败");
        log.info("修改阅读状态任务完成");
        return result;
    }
}