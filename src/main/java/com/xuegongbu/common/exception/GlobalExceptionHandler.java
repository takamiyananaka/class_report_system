package com.xuegongbu.common.exception;

import com.xuegongbu.common.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;

@Hidden
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * Sa-Token 登录认证异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLoginException(NotLoginException e) {
        log.error("Sa-Token登录认证异常：{}", e.getMessage());
        return Result.error(401, "当前登录状态已过期，请重新登录");
    }
    
    /**
     * Sa-Token 权限认证异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermissionException(NotPermissionException e) {
        log.error("Sa-Token权限认证异常：{}", e.getMessage());
        return Result.error(403, "权限不足，无法访问");
    }
    
    /**
     * Sa-Token 角色认证异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRoleException(NotRoleException e) {
        log.error("Sa-Token角色认证异常：{}", e.getMessage());
        return Result.error(403, "角色不符，无法访问");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error("系统异常，请联系管理员,异常信息："+e.getMessage());
    }
}